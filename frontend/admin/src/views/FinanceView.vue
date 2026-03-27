<template>
  <div class="finance-page">
    <!-- 收入概览 -->
    <div class="finance-section">
      <div class="section-header-row">
        <h2 class="section-h">收入概览</h2>
      </div>
      <div class="overview-grid">
        <div v-for="card in overviewCards" :key="card.key" class="overview-card">
          <div class="ov-top">
            <div class="ov-icon" :style="{ background: card.bg }">{{ card.emoji }}</div>
            <div class="ov-label">{{ card.label }}</div>
          </div>
          <div class="ov-val">{{ formatMoney(overview[card.key]) }}</div>
        </div>
      </div>
    </div>

    <!-- 今日数据 -->
    <div class="finance-section">
      <div class="section-header-row">
        <h2 class="section-h">今日数据</h2>
      </div>
      <div class="today-grid">
        <div v-for="t in todayCards" :key="t.key" class="today-card">
          <div class="today-val">{{ t.isCount ? (overview[t.key] || 0) : ('\u00a5' + formatMoney(overview[t.key])) }}</div>
          <div class="today-label">{{ t.label }}</div>
        </div>
      </div>
    </div>

    <!-- 收入构成 -->
    <div class="finance-section">
      <div class="section-header-row">
        <h2 class="section-h">收入构成</h2>
      </div>
      <div class="comp-row">
        <div class="comp-card">
          <div class="comp-title">平台收入构成</div>
          <div class="comp-chart">
            <div class="donut-wrapper">
              <svg viewBox="0 0 120 120" class="donut-svg">
                <circle cx="60" cy="60" r="50" fill="none" stroke="rgba(255,255,255,0.06)" stroke-width="12" />
                <circle cx="60" cy="60" r="50" fill="none" stroke="#6366f1" stroke-width="12"
                  :stroke-dasharray="membershipArc + ' ' + (314.16 - membershipArc)"
                  stroke-dashoffset="78.54" stroke-linecap="round" />
                <circle cx="60" cy="60" r="50" fill="none" stroke="#f5a623" stroke-width="12"
                  :stroke-dasharray="feeArc + ' ' + (314.16 - feeArc)"
                  :stroke-dashoffset="78.54 - membershipArc" stroke-linecap="round" />
              </svg>
              <div class="donut-center">
                <span class="donut-total">{{ formatShortMoney(overview.platformTotalIncome) }}</span>
                <span class="donut-sub">总计</span>
              </div>
            </div>
            <div class="comp-legend">
              <div class="legend-item">
                <span class="legend-dot" style="background:#6366f1"></span>
                <span class="legend-text">会员充值</span>
                <span class="legend-val">{{ formatMoney(overview.membershipIncome) }}</span>
              </div>
              <div class="legend-item">
                <span class="legend-dot" style="background:#f5a623"></span>
                <span class="legend-text">约稿服务费</span>
                <span class="legend-val">{{ formatMoney(overview.platformFeeIncome) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="comp-card">
          <div class="comp-title">交易类型分布</div>
          <div class="comp-bars">
            <div v-for="bar in distBars" :key="bar.label" class="dist-item">
              <div class="dist-label-row">
                <span class="dist-label">{{ bar.label }}</span>
                <span class="dist-count">{{ overview[bar.countKey] || 0 }} 笔</span>
                <span class="dist-amount">{{ formatMoney(overview[bar.amountKey]) }}</span>
              </div>
              <div class="dist-bar-bg">
                <div class="dist-bar-fill" :style="{ width: bar.pct + '%', background: bar.color }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 收入趋势 -->
    <div class="finance-section">
      <div class="section-header-row">
        <h2 class="section-h">收入趋势</h2>
        <div class="trend-tabs">
          <button v-for="t in trendTabs" :key="t.value" class="trend-tab"
            :class="{ active: trendPeriod === t.value }" @click="changeTrendPeriod(t.value)">
            {{ t.label }}
          </button>
        </div>
      </div>
      <div class="trend-chart-box">
        <div v-if="trendLoading" class="chart-loading"><div class="spin"></div></div>
        <div v-else class="chart-bars">
          <div class="chart-y-axis">
            <span v-for="(v, i) in yAxisLabels" :key="i" class="y-label">{{ v }}</span>
          </div>
          <div class="chart-body">
            <div class="chart-grid">
              <div v-for="i in 5" :key="i" class="grid-line"></div>
            </div>
            <div class="bar-container">
              <div v-for="(d, i) in displayTrendData" :key="i" class="bar-group"
                @mouseenter="showTooltip($event, d)" @mouseleave="hideTooltip">
                <div class="bar platform-bar" :style="{ height: getBarHeight(d.platformIncome) }"></div>
                <div class="bar artist-bar" :style="{ height: getBarHeight(d.artistIncome) }"></div>
                <span class="bar-label">{{ formatDateLabel(d.date) }}</span>
              </div>
            </div>
            <div v-if="tooltipVisible" class="chart-tooltip" :style="tooltipStyle">
              <div class="tt-date">{{ tooltipData.date }}</div>
              <div class="tt-row"><span class="tt-dot" style="background:#6366f1"></span>平台：{{ formatMoney(tooltipData.platformIncome) }}</div>
              <div class="tt-row"><span class="tt-dot" style="background:#34d399"></span>画师：{{ formatMoney(tooltipData.artistIncome) }}</div>
            </div>
          </div>
        </div>
        <div class="chart-legend">
          <span class="cl-item"><span class="cl-dot" style="background:#6366f1"></span>平台收入</span>
          <span class="cl-item"><span class="cl-dot" style="background:#34d399"></span>画师收入</span>
        </div>
      </div>
    </div>

    <!-- 最近交易 -->
    <div class="finance-section">
      <div class="section-header-row"><h2 class="section-h">最近交易</h2></div>
      <div class="table-wrapper">
        <div v-if="recentLoading" class="tbl-loading"><div class="spin"></div></div>
        <el-table v-else :data="recentTx" size="small">
          <el-table-column label="订单号" width="170" show-overflow-tooltip>
            <template #default="{ row }"><span class="mono-text">{{ row.orderNo?.slice(-12) || '\u2014' }}</span></template>
          </el-table-column>
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag :type="txTagType(row.paymentType)" size="small">{{ getTypeLabel(row.paymentType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="110">
            <template #default="{ row }"><span class="money">{{ formatMoney(row.amount) }}</span></template>
          </el-table-column>
          <el-table-column label="服务费" width="100">
            <template #default="{ row }">{{ row.platformFee ? formatMoney(row.platformFee) : '\u2014' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="txStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付时间" min-width="150">
            <template #default="{ row }"><span class="text-muted">{{ formatTime(row.paidAt) }}</span></template>
          </el-table-column>
        </el-table>
        <div v-if="!recentLoading && recentTx.length === 0" class="empty-state">
          <div class="empty-icon">📊</div><p>暂无交易记录</p>
        </div>
      </div>
    </div>

    <!-- 提现管理 -->
    <div class="finance-section">
      <div class="section-header-row">
        <h2 class="section-h">提现管理
          <span v-if="pendingWithdrawalCount > 0" class="pending-badge">{{ pendingWithdrawalCount }} 待审批</span>
        </h2>
        <div class="status-tabs">
          <button v-for="t in withdrawalStatusTabs" :key="t.value" class="status-tab"
            :class="{ active: withdrawalStatusFilter === t.value }" @click="changeWithdrawalFilter(t.value)">
            {{ t.label }}
          </button>
        </div>
      </div>
      <div class="table-wrapper">
        <div v-if="withdrawalLoading" class="tbl-loading"><div class="spin"></div></div>
        <el-table v-else :data="withdrawals">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userId" label="用户ID" width="90" />
          <el-table-column label="提现金额" width="120">
            <template #default="{ row }"><span class="money">{{ formatMoney(row.amount) }}</span></template>
          </el-table-column>
          <el-table-column label="支付宝账号" min-width="170">
            <template #default="{ row }"><span class="mono-text">{{ row.alipayAccount || '\u2014' }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="wdTagType(row.status)" size="small">{{ getWithdrawalStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="160">
            <template #default="{ row }"><span class="text-muted">{{ formatTime(row.createdAt) }}</span></template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button size="small" type="success" text @click="handleApprove(row)">通过</el-button>
                <el-button size="small" type="danger" text @click="handleReject(row)">拒绝</el-button>
              </template>
              <span v-else class="text-muted" style="font-size:12px">{{ row.processedAt ? formatTime(row.processedAt) : '\u2014' }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!withdrawalLoading && withdrawals.length === 0" class="empty-state">
          <div class="empty-icon">💸</div><p>暂无提现记录</p>
        </div>
        <div class="simple-pagination" v-if="withdrawalTotal > withdrawalPageSize">
          <el-button :disabled="withdrawalPage === 0" size="small" @click="withdrawalPage--; loadWithdrawals()">上一页</el-button>
          <span class="page-info">{{ withdrawalPage + 1 }} / {{ Math.ceil(withdrawalTotal / withdrawalPageSize) }}</span>
          <el-button :disabled="(withdrawalPage+1)*withdrawalPageSize >= withdrawalTotal" size="small" @click="withdrawalPage++; loadWithdrawals()">下一页</el-button>
        </div>
      </div>
    </div>

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝提现" width="440px">
      <p style="font-size:13px;color:var(--c-text-secondary);margin-bottom:14px">拒绝后金额将退回画师可用余额</p>
      <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="拒绝原因（选填）" />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
