package com.pixiv.artwork.service;

import com.pixiv.artwork.dto.*;
import com.pixiv.artwork.entity.*;
import com.pixiv.artwork.feign.MembershipServiceClient;
import com.pixiv.artwork.feign.UserServiceClient;
import com.pixiv.artwork.repository.*;
import com.pixiv.common.dto.PageResult;
import com.pixiv.common.dto.Result;
import com.pixiv.common.dto.UserDTO;
import com.pixiv.common.exception.BusinessException;
import com.pixiv.common.constant.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 比赛服务
 */
@Service
public class ContestService {

    private static final Logger logger = LoggerFactory.getLogger(ContestService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final ContestRepository contestRepository;
    private final ContestEntryRepository contestEntryRepository;
    private final ContestVoteRepository contestVoteRepository;
    private final MembershipServiceClient membershipServiceClient;
    private final UserServiceClient userServiceClient;

    public ContestService(ContestRepository contestRepository,
            ContestEntryRepository contestEntryRepository,
            ContestVoteRepository contestVoteRepository,
            MembershipServiceClient membershipServiceClient,
            UserServiceClient userServiceClient) {
        this.contestRepository = contestRepository;
        this.contestEntryRepository = contestEntryRepository;
        this.contestVoteRepository = contestVoteRepository;
        this.membershipServiceClient = membershipServiceClient;
        this.userServiceClient = userServiceClient;
    }

    // ==================== 管理员操作 ====================

    /**
     * 创建比赛
     */
    @Transactional
    public ContestDTO createContest(ContestRequest request) {
        logger.info("创建比赛: title={}", request.getTitle());

        Contest contest = new Contest();
        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setCoverImage(request.getCoverImage());
        contest.setRules(request.getRules());
        contest.setRewardInfo(request.getRewardInfo());
        contest.setStartTime(parseDateTime(request.getStartTime()));
        contest.setEndTime(parseDateTime(request.getEndTime()));
        contest.setVotingEndTime(parseDateTime(request.getVotingEndTime()));
        contest.setMaxEntriesPerArtist(request.getMaxEntriesPerArtist() != null ? request.getMaxEntriesPerArtist() : 1);

        // 根据时间自动设置状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(contest.getStartTime())) {
            contest.setStatus(ContestStatus.UPCOMING);
        } else if (now.isBefore(contest.getEndTime())) {
            contest.setStatus(ContestStatus.ACTIVE);
        } else if (now.isBefore(contest.getVotingEndTime())) {
            contest.setStatus(ContestStatus.VOTING);
        } else {
            contest.setStatus(ContestStatus.ENDED);
        }

        contest = contestRepository.save(contest);
        logger.info("比赛创建成功: contestId={}", contest.getId());
        return new ContestDTO(contest);
    }

    /**
     * 更新比赛
     */
    @Transactional
    public ContestDTO updateContest(Long contestId, ContestRequest request) {
        logger.info("更新比赛: contestId={}", contestId);

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "比赛不存在"));

        if (request.getTitle() != null)
            contest.setTitle(request.getTitle());
        if (request.getDescription() != null)
            contest.setDescription(request.getDescription());
        if (request.getCoverImage() != null)
            contest.setCoverImage(request.getCoverImage());
        if (request.getRules() != null)
            contest.setRules(request.getRules());
        if (request.getRewardInfo() != null)
            contest.setRewardInfo(request.getRewardInfo());
        if (request.getStartTime() != null)
            contest.setStartTime(parseDateTime(request.getStartTime()));
        if (request.getEndTime() != null)
            contest.setEndTime(parseDateTime(request.getEndTime()));
        if (request.getVotingEndTime() != null)
            contest.setVotingEndTime(parseDateTime(request.getVotingEndTime()));
        if (request.getMaxEntriesPerArtist() != null)
            contest.setMaxEntriesPerArtist(request.getMaxEntriesPerArtist());

        contest = contestRepository.save(contest);
        return new ContestDTO(contest);
    }

    /**
     * 更新比赛状态
     */
    @Transactional
    public ContestDTO updateContestStatus(Long contestId, String status) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "比赛不存在"));

        contest.setStatus(ContestStatus.valueOf(status));

        // 如果设为 ENDED，自动计算排名
        if (ContestStatus.ENDED.name().equals(status)) {
            calculateRankings(contestId);
        }

        contest = contestRepository.save(contest);
        return new ContestDTO(contest);
    }

    /**
     * 获取所有比赛（管理员视角，分页）
     */
    @Transactional
    public PageResult<ContestDTO> getAllContests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Contest> contests = contestRepository.findAll(pageable);
        contests.getContent().forEach(this::autoUpdateStatus);
        return toPageResult(contests);
    }

    /**
     * 获取首页精选参赛作品（来自进行中/投票中的比赛的高分作品）
     */
    @Transactional(readOnly = true)
    public List<ContestEntryDTO> getFeaturedEntries(int limit) {
        // 获取进行中和投票中的比赛
        List<Contest> activeContests = contestRepository.findByStatusIn(
                Arrays.asList(ContestStatus.ACTIVE, ContestStatus.VOTING));

        if (activeContests.isEmpty()) {
            // 如果没有进行中的，取最近结束的比赛
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "votingEndTime"));
            Page<Contest> endedPage = contestRepository.findByStatus(ContestStatus.ENDED, pageable);
            activeContests = endedPage.getContent();
        }

        if (activeContests.isEmpty()) {
            return List.of();
        }

        List<Long> contestIds = activeContests.stream().map(Contest::getId).collect(Collectors.toList());

        // 获取这些比赛的高分参赛作品
        Pageable pageable = PageRequest.of(0, limit);
        List<ContestEntry> entries = contestEntryRepository
                .findByContestIdInAndStatusOrderByAverageScoreDesc(contestIds, ContestEntryStatus.APPROVED, pageable);

        // 构建映射：contestId -> contestTitle
        Map<Long, String> contestTitleMap = activeContests.stream()
                .collect(Collectors.toMap(Contest::getId, Contest::getTitle));

        return entries.stream()
                .map(entry -> {
                    ContestEntryDTO dto = new ContestEntryDTO(entry);
                    dto.setContestTitle(contestTitleMap.getOrDefault(entry.getContestId(), ""));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 删除比赛
     */
    @Transactional
    public void deleteContest(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "比赛不存在"));
        contestRepository.delete(contest);
        logger.info("比赛已删除: contestId={}", contestId);
    }

    /**
     * 审核参赛作品
     */
    @Transactional
    public ContestEntryDTO reviewEntry(Long entryId, String status) {
        ContestEntry entry = contestEntryRepository.findById(entryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "参赛作品不存在"));

        entry.setStatus(ContestEntryStatus.valueOf(status));
        entry = contestEntryRepository.save(entry);
        return new ContestEntryDTO(entry);
    }

    // ==================== 用户/画师操作 ====================

    /**
     * 获取比赛列表（用户视角）
     */
    @Transactional
    public PageResult<ContestDTO> getContests(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Contest> contests;

        if (status != null && !status.isEmpty()) {
            contests = contestRepository.findByStatus(ContestStatus.valueOf(status), pageable);
        } else {
            // 默认显示非取消的比赛
            contests = contestRepository.findByStatusIn(
                    Arrays.asList(ContestStatus.UPCOMING, ContestStatus.ACTIVE, ContestStatus.VOTING,
                            ContestStatus.ENDED),
                    pageable);
        }

        // 自动更新每个比赛的状态
        contests.getContent().forEach(this::autoUpdateStatus);

        return toPageResult(contests);
    }

    /**
     * 获取比赛详情
     */
    @Transactional
    public ContestDTO getContest(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "比赛不存在"));

        // 自动更新状态
        autoUpdateStatus(contest);

        return new ContestDTO(contest);
    }

    /**
     * 提交参赛作品
     */
    @Transactional
    public ContestEntryDTO submitEntry(Long contestId, Long artistId, String artistName, SubmitEntryRequest request) {
        logger.info("提交参赛作品: contestId={}, artistId={}", contestId, artistId);

        // 1. 检查比赛状态
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "比赛不存在"));

        autoUpdateStatus(contest);
        if (contest.getStatus() != ContestStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "比赛当前不接受投稿");
        }

        // 2. 检查是否已参赛
        int existingCount = contestEntryRepository.countByContestIdAndArtistId(contestId, artistId);
        if (existingCount >= contest.getMaxEntriesPerArtist()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "您已达到该比赛的最大参赛数量");
        }

        // 3. 创建参赛作品
        ContestEntry entry = new ContestEntry();
        entry.setContestId(contestId);
        entry.setArtistId(artistId);
        entry.setArtistName(artistName);
        entry.setTitle(request.getTitle());
        entry.setDescription(request.getDescription());
        entry.setImageUrl(request.getImageUrl());
        entry.setThumbnailUrl(request.getThumbnailUrl());
        entry.setStatus(ContestEntryStatus.APPROVED); // 默认自动通过

        entry = contestEntryRepository.save(entry);

        // 4. 更新比赛参赛数
        contest.incrementEntryCount();
        contestRepository.save(contest);

        logger.info("参赛作品提交成功: entryId={}", entry.getId());
        return new ContestEntryDTO(entry);
    }

    /**
     * 获取比赛的参赛作品列表（排行榜）
     */
    @Transactional(readOnly = true)
    public PageResult<ContestEntryDTO> getEntries(Long contestId, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContestEntry> entries = contestEntryRepository
                .findByContestIdAndStatusOrderByAverageScoreDesc(contestId, ContestEntryStatus.APPROVED, pageable);

        List<ContestEntryDTO> dtos = entries.getContent().stream()
                .map(entry -> {
                    ContestEntryDTO dto = new ContestEntryDTO(entry);
                    // 通过user-service补充画师信息（真实用户名和头像）
                    try {
                        Result<UserDTO> userResult = userServiceClient.getUserById(entry.getArtistId());
                        if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                            dto.setArtistName(userResult.getData().getUsername());
                            dto.setArtistAvatar(userResult.getData().getAvatarUrl());
                        }
                    } catch (Exception e) {
                        logger.warn("获取画师信息失败: artistId={}", entry.getArtistId());
                    }
                    // 检查当前用户是否已投票
                    if (currentUserId != null) {
                        contestVoteRepository.findByEntryIdAndVoterId(entry.getId(), currentUserId)
                                .ifPresentOrElse(
                                        vote -> {
                                            dto.setHasVoted(true);
                                            dto.setMyScore(vote.getScore());
                                        },
                                        () -> dto.setHasVoted(false));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        PageResult<ContestEntryDTO> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(entries.getTotalElements());
        result.setPage(entries.getNumber() + 1);
        result.setSize(entries.getSize());
        result.setPages(entries.getTotalPages());
        result.setHasPrevious(entries.hasPrevious());
        result.setHasNext(entries.hasNext());
        return result;
    }

    /**
     * 投票
     */
    @Transactional
    public ContestEntryDTO vote(Long contestId, Long voterId, VoteRequest request) {
        logger.info("投票: contestId={}, entryId={}, voterId={}, score={}", contestId, request.getEntryId(), voterId,
                request.getScore());

        // 1. 检查比赛状态
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "比赛不存在"));

        autoUpdateStatus(contest);
        if (contest.getStatus() != ContestStatus.ACTIVE && contest.getStatus() != ContestStatus.VOTING) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "比赛当前不接受投票");
        }

        // 2. 检查参赛作品
        ContestEntry entry = contestEntryRepository.findById(request.getEntryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "参赛作品不存在"));

        if (!entry.getContestId().equals(contestId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参赛作品不属于该比赛");
        }

        // 3. 不能给自己投票
        if (entry.getArtistId().equals(voterId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能给自己的作品投票");
        }

        // 4. 检查是否已投票
        if (contestVoteRepository.existsByEntryIdAndVoterId(request.getEntryId(), voterId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "您已对该作品投过票");
        }

        // 5. 创建投票
        ContestVote vote = new ContestVote();
        vote.setContestId(contestId);
        vote.setEntryId(request.getEntryId());
        vote.setVoterId(voterId);
        vote.setScore(request.getScore());
        vote.setComment(request.getComment());
        contestVoteRepository.save(vote);

        // 6. 更新参赛作品评分
        entry.addVote(request.getScore(), 1.0);
        entry = contestEntryRepository.save(entry);

        logger.info("投票成功: entryId={}, newAvg={}", entry.getId(), entry.getAverageScore());

        ContestEntryDTO dto = new ContestEntryDTO(entry);
        dto.setHasVoted(true);
        dto.setMyScore(request.getScore());
        return dto;
    }

    /**
     * 获取某参赛作品的投票列表
     */
    @Transactional(readOnly = true)
    public PageResult<ContestVoteDTO> getVotes(Long entryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContestVote> votes = contestVoteRepository.findByEntryIdOrderByCreatedAtDesc(entryId, pageable);

        List<ContestVoteDTO> dtos = votes.getContent().stream()
                .map(vote -> {
                    ContestVoteDTO dto = new ContestVoteDTO(vote);
                    // 通过user-service补充投票者信息
                    try {
                        Result<UserDTO> userResult = userServiceClient.getUserById(vote.getVoterId());
                        if (userResult != null && userResult.isSuccess() && userResult.getData() != null) {
                            dto.setVoterName(userResult.getData().getUsername());
                            dto.setVoterAvatar(userResult.getData().getAvatarUrl());
                        }
                    } catch (Exception e) {
                        logger.warn("获取投票者信息失败: voterId={}", vote.getVoterId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        PageResult<ContestVoteDTO> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(votes.getTotalElements());
        result.setPage(votes.getNumber() + 1);
        result.setSize(votes.getSize());
        result.setPages(votes.getTotalPages());
        result.setHasPrevious(votes.hasPrevious());
        result.setHasNext(votes.hasNext());
        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 自动根据时间更新比赛状态（只会向前推进，不会回退管理员手动设置的状态）
     */
    private void autoUpdateStatus(Contest contest) {
        LocalDateTime now = LocalDateTime.now();
        ContestStatus currentStatus = contest.getStatus();

        // 已取消或已结束的比赛不自动更新
        if (currentStatus == ContestStatus.CANCELLED || currentStatus == ContestStatus.ENDED) {
            return;
        }

        // 根据时间计算应该处于的状态
        ContestStatus timeBasedStatus;
        if (now.isBefore(contest.getStartTime())) {
            timeBasedStatus = ContestStatus.UPCOMING;
        } else if (now.isBefore(contest.getEndTime())) {
            timeBasedStatus = ContestStatus.ACTIVE;
        } else if (now.isBefore(contest.getVotingEndTime())) {
            timeBasedStatus = ContestStatus.VOTING;
        } else {
            timeBasedStatus = ContestStatus.ENDED;
        }

        // 状态优先级: UPCOMING(0) < ACTIVE(1) < VOTING(2) < ENDED(3)
        // 只允许向前推进，不允许回退（尊重管理员手动设置的更高状态）
        int currentOrdinal = statusOrdinal(currentStatus);
        int timeOrdinal = statusOrdinal(timeBasedStatus);

        if (timeOrdinal > currentOrdinal) {
            contest.setStatus(timeBasedStatus);
            contestRepository.save(contest);
            logger.info("比赛状态自动更新: contestId={}, {} -> {}", contest.getId(), currentStatus, timeBasedStatus);

            // 如果刚进入 ENDED 状态，自动计算排名
            if (timeBasedStatus == ContestStatus.ENDED) {
                calculateRankings(contest.getId());
            }
        }
    }

    /**
     * 获取状态的优先级序号
     */
    private int statusOrdinal(ContestStatus status) {
        switch (status) {
            case UPCOMING:
                return 0;
            case ACTIVE:
                return 1;
            case VOTING:
                return 2;
            case ENDED:
                return 3;
            case CANCELLED:
                return -1;
            default:
                return 0;
        }
    }

    /**
     * 计算排名
     */
    @Transactional
    public void calculateRankings(Long contestId) {
        logger.info("计算比赛排名: contestId={}", contestId);

        List<ContestEntry> entries = contestEntryRepository
                .findByContestIdAndStatusOrderByAverageScoreDesc(contestId, ContestEntryStatus.APPROVED);

        int rank = 1;
        for (ContestEntry entry : entries) {
            entry.setRankPosition(rank);
            if (rank <= 3) {
                entry.setStatus(ContestEntryStatus.WINNER);
            }
            rank++;
        }

        contestEntryRepository.saveAll(entries);
        logger.info("排名计算完成: contestId={}, totalEntries={}", contestId, entries.size());
    }

    private PageResult<ContestDTO> toPageResult(Page<Contest> contests) {
        List<ContestDTO> dtos = contests.getContent().stream()
                .map(ContestDTO::new)
                .collect(Collectors.toList());

        PageResult<ContestDTO> result = new PageResult<>();
        result.setRecords(dtos);
        result.setTotal(contests.getTotalElements());
        result.setPage(contests.getNumber() + 1);
        result.setSize(contests.getSize());
        result.setPages(contests.getTotalPages());
        result.setHasPrevious(contests.hasPrevious());
        result.setHasNext(contests.hasNext());
        return result;
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null)
            return null;
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (Exception e) {
            // 尝试其他格式
            try {
                return LocalDateTime.parse(dateTime);
            } catch (Exception e2) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "时间格式不正确，请使用 yyyy-MM-ddTHH:mm:ss 格式");
            }
        }
    }
}
