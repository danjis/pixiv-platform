package com.pixiv.user.service;

import com.pixiv.user.entity.ArtistWallet;
import com.pixiv.user.entity.WalletTransaction;
import com.pixiv.user.entity.WithdrawalRequest;
import com.pixiv.user.entity.WithdrawalRequest.WithdrawalStatus;
import com.pixiv.user.feign.PaymentServiceClient;
import com.pixiv.user.repository.ArtistWalletRepository;
import com.pixiv.user.repository.WalletTransactionRepository;
import com.pixiv.user.repository.WithdrawalRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 画师钱包服务
 * 管理画师收入余额和交易记录
 */
@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final ArtistWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final PaymentServiceClient paymentServiceClient;

    public WalletService(ArtistWalletRepository walletRepository,
            WalletTransactionRepository transactionRepository,
            WithdrawalRequestRepository withdrawalRequestRepository,
            PaymentServiceClient paymentServiceClient) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.paymentServiceClient = paymentServiceClient;
    }

    /**
     * 获取或创建画师钱包
     */
    @Transactional
    public ArtistWallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    ArtistWallet wallet = new ArtistWallet();
                    wallet.setUserId(userId);
                    return walletRepository.save(wallet);
                });
    }

    /**
     * 获取钱包概览数据
     */
    @Transactional
    public Map<String, Object> getWalletOverview(Long userId) {
        ArtistWallet wallet = getOrCreateWallet(userId);

        // 本月收入
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        BigDecimal monthIncome = transactionRepository.sumIncomeByUserIdAndPeriod(userId, monthStart, monthEnd);

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalIncome", wallet.getTotalIncome());
        overview.put("availableBalance", wallet.getAvailableBalance());
        overview.put("frozenAmount", wallet.getFrozenAmount());
        overview.put("withdrawnAmount", wallet.getWithdrawnAmount());
        overview.put("monthIncome", monthIncome);
        return overview;
    }

    /**
     * 获取交易记录
     */
    @Transactional(readOnly = true)
    public Page<WalletTransaction> getTransactions(Long userId, int page, int size) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    /**
     * 收入入账（约稿完成时调用）
     *
     * @param userId       画师用户ID
     * @param amount       入账金额
     * @param commissionId 关联约稿ID
     * @param orderNo      关联支付订单号
     * @param description  描述
     */
    @Transactional
    public void addIncome(Long userId, BigDecimal amount, Long commissionId, String orderNo, String description) {
        logger.info("画师收入入账: userId={}, amount={}, commissionId={}", userId, amount, commissionId);

        ArtistWallet wallet = getOrCreateWallet(userId);

        // 更新钱包余额
        wallet.setTotalIncome(wallet.getTotalIncome().add(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        walletRepository.save(wallet);

        // 记录交易
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(WalletTransaction.TransactionType.INCOME);
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getAvailableBalance());
        tx.setDescription(description);
        tx.setCommissionId(commissionId);
        tx.setOrderNo(orderNo);
        transactionRepository.save(tx);

        logger.info("收入入账成功: userId={}, newBalance={}", userId, wallet.getAvailableBalance());
    }

    /**
     * 冻结金额（定金支付成功时调用）
     */
    @Transactional
    public void freezeAmount(Long userId, BigDecimal amount, Long commissionId, String description) {
        logger.info("冻结金额: userId={}, amount={}, commissionId={}", userId, amount, commissionId);

        ArtistWallet wallet = getOrCreateWallet(userId);
        wallet.setFrozenAmount(wallet.getFrozenAmount().add(amount));
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(WalletTransaction.TransactionType.FREEZE);
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getAvailableBalance());
        tx.setDescription(description);
        tx.setCommissionId(commissionId);
        transactionRepository.save(tx);
    }

    /**
     * 解冻金额并转为可用（约稿完成确认时调用）
     */
    @Transactional
    public void unfreezeAndRelease(Long userId, BigDecimal amount, Long commissionId, String description) {
        logger.info("解冻并释放: userId={}, amount={}, commissionId={}", userId, amount, commissionId);

        ArtistWallet wallet = getOrCreateWallet(userId);
        wallet.setFrozenAmount(wallet.getFrozenAmount().subtract(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        wallet.setTotalIncome(wallet.getTotalIncome().add(amount));
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(WalletTransaction.TransactionType.UNFREEZE);
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getAvailableBalance());
        tx.setDescription(description);
        tx.setCommissionId(commissionId);
        transactionRepository.save(tx);
    }

    /**
     * 取消冻结（退款时调用，仅减少冻结金额，不入可用余额）
     * 与 unfreezeAndRelease 的区别：此方法不增加可用余额和总收入，
     * 因为退款场景下资金退还给委托方而非画师
     */
    @Transactional
    public void cancelFreeze(Long userId, BigDecimal amount, Long commissionId, String description) {
        logger.info("取消冻结(退款): userId={}, amount={}, commissionId={}", userId, amount, commissionId);

        ArtistWallet wallet = getOrCreateWallet(userId);
        BigDecimal newFrozen = wallet.getFrozenAmount().subtract(amount);
        if (newFrozen.compareTo(BigDecimal.ZERO) < 0) {
            newFrozen = BigDecimal.ZERO;
        }
        wallet.setFrozenAmount(newFrozen);
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(WalletTransaction.TransactionType.REFUND);
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getAvailableBalance());
        tx.setDescription(description);
        tx.setCommissionId(commissionId);
        transactionRepository.save(tx);
    }

    /**
     * 提现申请（从可用余额扣除，创建审批记录）
     * 余额立即扣除，管理员审批后确认打款；拒绝则退回余额
     */
    @Transactional
    public WithdrawalRequest withdraw(Long userId, BigDecimal amount, String alipayAccount, String alipayName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("提现金额必须大于0");
        }
        if (alipayAccount == null || alipayAccount.trim().isEmpty()) {
            throw new IllegalArgumentException("请填写支付宝账号");
        }
        if (alipayName == null || alipayName.trim().isEmpty()) {
            throw new IllegalArgumentException("请填写支付宝实名姓名");
        }

        ArtistWallet wallet = getOrCreateWallet(userId);
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("可用余额不足，当前余额: " + wallet.getAvailableBalance());
        }

        // 扣除可用余额（先冻结，等审批通过后才算真正提现）
        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        walletRepository.save(wallet);

        // 创建提现申请记录
        WithdrawalRequest request = new WithdrawalRequest();
        request.setUserId(userId);
        request.setAmount(amount);
        request.setAlipayAccount(alipayAccount);
        request.setAlipayName(alipayName);
        request.setStatus(WithdrawalStatus.PENDING);
        withdrawalRequestRepository.save(request);

        // 记录交易
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(WalletTransaction.TransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getAvailableBalance());
        tx.setDescription("提现申请 ¥" + amount + " → " + alipayAccount + "（待审批）");
        transactionRepository.save(tx);

        logger.info("提现申请已提交: userId={}, amount={}, withdrawalId={}", userId, amount, request.getId());
        return request;
    }

    /**
     * 管理员审批通过提现
     */
    @Transactional
    public void approveWithdrawal(Long withdrawalId, Long adminId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new IllegalArgumentException("提现申请不存在"));
        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalArgumentException("该申请已处理");
        }

        // 调用支付宝转账API实际打款
        String outBizNo = "WITHDRAW" + withdrawalId + "_" + System.currentTimeMillis();
        Map<String, String> transferBody = new HashMap<>();
        transferBody.put("outBizNo", outBizNo);
        transferBody.put("amount", request.getAmount().toPlainString());
        transferBody.put("alipayAccount", request.getAlipayAccount());
        transferBody.put("alipayName", request.getAlipayName());

        try {
            var result = paymentServiceClient.transferToAlipay(transferBody);
            if (result == null || !result.isSuccess()) {
                String msg = result != null ? result.getMessage() : "调用转账服务失败";
                throw new RuntimeException("支付宝转账失败: " + msg);
            }
            logger.info("提现转账成功: withdrawalId={}, alipayOrderId={}", withdrawalId, result.getData());
        } catch (Exception e) {
            logger.error("提现转账失败: withdrawalId={}, alipayAccount={}", withdrawalId, request.getAlipayAccount(), e);
            throw new RuntimeException("支付宝打款失败，请稍后重试: " + e.getMessage());
        }

        request.setStatus(WithdrawalStatus.APPROVED);
        request.setProcessedBy(adminId);
        request.setProcessedAt(java.time.LocalDateTime.now());
        withdrawalRequestRepository.save(request);

        // 更新已提现累计金额
        ArtistWallet wallet = getOrCreateWallet(request.getUserId());
        wallet.setWithdrawnAmount(wallet.getWithdrawnAmount().add(request.getAmount()));
        walletRepository.save(wallet);

        logger.info("提现审批通过: withdrawalId={}, userId={}, amount={}", withdrawalId, request.getUserId(),
                request.getAmount());
    }

    /**
     * 管理员拒绝提现（退回金额到可用余额）
     */
    @Transactional
    public void rejectWithdrawal(Long withdrawalId, Long adminId, String remark) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new IllegalArgumentException("提现申请不存在"));
        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalArgumentException("该申请已处理");
        }

        request.setStatus(WithdrawalStatus.REJECTED);
        request.setProcessedBy(adminId);
        request.setProcessedAt(java.time.LocalDateTime.now());
        request.setRemark(remark);
        withdrawalRequestRepository.save(request);

        // 退回金额到可用余额
        ArtistWallet wallet = getOrCreateWallet(request.getUserId());
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        // 记录退回交易
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(request.getUserId());
        tx.setType(WalletTransaction.TransactionType.INCOME);
        tx.setAmount(request.getAmount());
        tx.setBalanceAfter(wallet.getAvailableBalance());
        tx.setDescription("提现被拒绝，退回余额 ¥" + request.getAmount() + (remark != null ? "（" + remark + "）" : ""));
        transactionRepository.save(tx);

        logger.info("提现审批拒绝: withdrawalId={}, userId={}, amount={}", withdrawalId, request.getUserId(),
                request.getAmount());
    }

    /**
     * 获取提现申请列表（管理员）
     */
    public Page<WithdrawalRequest> getWithdrawals(String status, int page, int size) {
        if (status != null && !status.isEmpty()) {
            return withdrawalRequestRepository.findByStatusOrderByCreatedAtDesc(
                    WithdrawalStatus.valueOf(status), PageRequest.of(page, size));
        }
        return withdrawalRequestRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    /**
     * 获取画师自己的提现记录
     */
    public Page<WithdrawalRequest> getMyWithdrawals(Long userId, int page, int size) {
        return withdrawalRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    /**
     * 获取待审批提现数量
     */
    public long getPendingWithdrawalCount() {
        return withdrawalRequestRepository.countByStatus(WithdrawalStatus.PENDING);
    }
}
