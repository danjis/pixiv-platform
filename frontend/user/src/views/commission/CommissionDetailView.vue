<template>
  <div class="commission-detail-page">
    <div class="page-glow glow-left"></div>
    <div class="page-glow glow-right"></div>
    <div class="page-grid"></div>

    <div v-if="commission" class="page-container">
      <button class="back-btn" @click="router.back()">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M5 12l6-6M5 12l6 6" />
        </svg>
        返回列表
      </button>

      <section class="hero-panel surface-card">
        <div class="hero-copy">
          <div class="hero-kicker">
            <span class="kicker-dot"></span>
            创作协作单 #{{ commission.id }}
          </div>
          <div class="hero-title-row">
            <h1>{{ commission.title }}</h1>
            <span class="status-badge" :class="'status-' + commission.status">
              {{ statusMap[commission.status] || commission.status }}
            </span>
          </div>
          <p class="hero-desc">
            {{ isArtist
              ? '把报价、创作、交付和委托沟通集中到同一个工作台里，推进节奏会更清晰。'
              : '在这里查看报价、支付、交付与沟通记录，整张约稿单会持续沉淀。' }}
          </p>
          <div class="hero-meta">
            <span class="hero-meta-item">创建于 {{ formatTime(commission.createdAt) }}</span>
            <span class="hero-meta-item">协作对象：{{ otherUser?.nickname || (isArtist ? '委托方' : '画师') }}</span>
            <span class="hero-meta-item">消息 {{ messages.length }} 条</span>
          </div>
        </div>

        <div class="hero-stats">
          <div class="hero-stat-card">
            <span class="hero-stat-label">预算参考</span>
            <strong class="hero-stat-value">{{ commission.budget ? '¥' + commission.budget : '待沟通' }}</strong>
            <span class="hero-stat-note">委托提交时给出的预算区间参考</span>
          </div>
          <div class="hero-stat-card">
            <span class="hero-stat-label">当前阶段</span>
            <strong class="hero-stat-value">{{ statusMap[commission.status] || commission.status }}</strong>
            <span class="hero-stat-note">当前状态会决定后续可用操作</span>
          </div>
          <div class="hero-stat-card">
            <span class="hero-stat-label">已支付记录</span>
            <strong class="hero-stat-value">{{ payments.filter((item) => item.status === 'PAID').length }} 笔</strong>
            <span class="hero-stat-note">支付与退款轨迹统一保留在本页</span>
          </div>
        </div>
      </section>

      <section class="insight-strip">
        <div class="insight-chip">
          <span class="insight-chip-label">当前身份</span>
          <strong>{{ isArtist ? '画师执行方' : '委托发起方' }}</strong>
        </div>
        <div class="insight-chip">
          <span class="insight-chip-label">参考素材</span>
          <strong>{{ refUrlList.length }} 条链接</strong>
        </div>
        <div class="insight-chip">
          <span class="insight-chip-label">售后状态</span>
          <strong>{{ hasOpenAfterSale ? '处理中' : '暂无进行中申请' }}</strong>
        </div>
      </section>

      <div class="detail-layout">
        <div class="main-col">
          <section class="surface-card section-card">
            <div class="section-head">
              <div>
                <p class="section-eyebrow">Creative Brief</p>
                <h2 class="section-title">约稿需求说明</h2>
              </div>
              <div class="section-badges">
                <span v-if="commission.deadline" class="soft-pill">预计交付 {{ formatDate(commission.deadline) }}</span>
                <span v-if="commission.revisionsAllowed" class="soft-pill">可修改 {{ commission.revisionsAllowed }} 次</span>
              </div>
            </div>

            <p class="desc-text">{{ commission.description || '当前还没有补充更详细的需求说明。' }}</p>

            <div v-if="refUrlList.length" class="reference-panel">
              <div class="mini-title-row">
                <h3>参考链接</h3>
                <span>{{ refUrlList.length }} 条</span>
              </div>
              <div class="reference-list">
                <a
                  v-for="(url, index) in refUrlList"
                  :key="index"
                  class="reference-link"
                  :href="url"
                  target="_blank"
                  rel="noopener"
                >
                  <span class="reference-index">{{ String(index + 1).padStart(2, '0') }}</span>
                  <span class="reference-url">{{ url }}</span>
                </a>
              </div>
            </div>
          </section>

          <section v-if="hasQuote" class="surface-card section-card">
            <div class="section-head">
              <div>
                <p class="section-eyebrow">Quote</p>
                <h2 class="section-title">报价与交付约定</h2>
              </div>
              <span class="soft-pill accent-pill">已进入执行阶段</span>
            </div>

            <div class="quote-grid">
              <div class="metric-card">
                <span class="metric-label">报价总额</span>
                <strong class="metric-value price">¥{{ commission.totalAmount }}</strong>
              </div>
              <div class="metric-card">
                <span class="metric-label">定金比例</span>
                <strong class="metric-value">{{ Math.round((commission.depositRatio || 0.3) * 100) }}%</strong>
              </div>
              <div class="metric-card">
                <span class="metric-label">定金金额</span>
                <strong class="metric-value">¥{{ commission.depositAmount }}</strong>
              </div>
              <div v-if="commission.totalAmount && commission.depositAmount" class="metric-card">
                <span class="metric-label">尾款金额</span>
                <strong class="metric-value">¥{{ (commission.totalAmount - commission.depositAmount).toFixed(2) }}</strong>
              </div>
              <div v-if="commission.revisionsAllowed" class="metric-card">
                <span class="metric-label">允许修改</span>
                <strong class="metric-value">{{ commission.revisionsAllowed }} 次</strong>
              </div>
              <div v-if="commission.deadline" class="metric-card">
                <span class="metric-label">预计交付</span>
                <strong class="metric-value">{{ formatDate(commission.deadline) }}</strong>
              </div>
            </div>

            <div v-if="commission.quoteNote" class="note-panel">
              <span class="note-badge">报价说明</span>
              <p>{{ commission.quoteNote }}</p>
            </div>
          </section>

          <section v-if="payments.length" class="surface-card section-card">
            <div class="section-head">
              <div>
                <p class="section-eyebrow">Payments</p>
                <h2 class="section-title">支付与退款记录</h2>
              </div>
              <span class="soft-pill">{{ payments.length }} 笔流水</span>
            </div>

            <div class="payment-list">
              <div v-for="p in payments" :key="p.id" class="payment-item">
                <div class="payment-top">
                  <div class="payment-primary">
                    <span class="payment-type">{{ formatPaymentType(p.paymentType) }}</span>
                    <strong class="payment-amount">¥{{ p.amount }}</strong>
                    <span v-if="p.discountAmount && p.discountAmount > 0" class="payment-coupon-tag">
                      优惠券减免 ¥{{ p.discountAmount }}
                    </span>
                  </div>
                  <span class="payment-status" :class="'ps-' + p.status">
                    {{ p.status === 'PENDING' && isOrderExpired(p) ? '已超时' : (paymentStatusMap[p.status] || p.status) }}
                  </span>
                </div>

                <div class="payment-meta">
                  <span v-if="p.orderNo">订单号 {{ p.orderNo }}</span>
                  <span v-if="p.paidAt">支付于 {{ formatTime(p.paidAt) }}</span>
                  <span v-if="p.refundedAt" class="refund-time">退款于 {{ formatTime(p.refundedAt) }}</span>
                  <span v-if="p.refundReason" class="refund-reason">退款原因：{{ p.refundReason }}</span>
                </div>

                <div v-if="p.status === 'PENDING' && p.payerId === currentUserId" class="payment-actions">
                  <button v-if="!isOrderExpired(p)" class="tiny-action-btn" @click="handleContinuePay(p.orderNo)">
                    继续支付
                  </button>
                  <span v-if="!isOrderExpired(p) && p.expireAt" class="expire-hint">{{ formatCountdown(p.expireAt) }}</span>
                </div>
              </div>
            </div>
          </section>

          <section v-if="afterSaleLoading || afterSales.length" class="surface-card section-card">
            <div class="section-head">
              <div>
                <p class="section-eyebrow">After Sale</p>
                <h2 class="section-title">售后与申诉进度</h2>
              </div>
              <span class="soft-pill">{{ afterSales.length }} 条申请</span>
            </div>

            <div v-if="afterSaleLoading" class="empty-block">正在加载售后记录...</div>
            <div v-else class="after-sale-list">
              <article v-for="item in afterSales" :key="item.id" class="after-sale-item">
                <div class="after-sale-top">
                  <div>
                    <h3>{{ item.title || getAfterSaleActionLabel(item) }}</h3>
                    <p>{{ getAfterSaleActionLabel(item) }}</p>
                  </div>
                  <span class="after-sale-status">{{ getAfterSaleSummary(item) }}</span>
                </div>
                <p class="after-sale-content">{{ item.content || '已提交售后申请，等待平台处理。' }}</p>
                <div class="after-sale-meta">
                  <span>创建于 {{ formatTime(item.createdAt) }}</span>
                  <span v-if="getAfterSaleResolutionLabel(item)">处理结果：{{ getAfterSaleResolutionLabel(item) }}</span>
                </div>
              </article>
            </div>
          </section>

          <section
            v-if="commission.status === 'CANCELLED' && commission.cancelledByRole"
            class="surface-card section-card cancel-info-card"
          >
            <div class="section-head">
              <div>
                <p class="section-eyebrow">Cancellation</p>
                <h2 class="section-title">取消信息</h2>
              </div>
              <div class="cancel-role-badge" :class="'role-' + commission.cancelledByRole">
                {{ cancelRoleLabelMap[commission.cancelledByRole] || commission.cancelledByRole }}
              </div>
            </div>

            <p v-if="commission.cancelReason" class="cancel-reason">{{ commission.cancelReason }}</p>

            <div class="cancel-refund-hint">
              <p v-if="commission.cancelledByRole === 'ARTIST'" class="refund-ok">
                画师取消后，已支付款项会按规则退回给委托方。
              </p>
              <p v-else-if="commission.cancelledByRole === 'CLIENT'" class="refund-partial">
                委托方主动取消时，定金通常不退回，尾款如已支付会进入退款流程。
              </p>
              <p v-else-if="commission.cancelledByRole === 'ADMIN'" class="refund-admin">
                管理员已介入处理，退款与后续说明以平台审核结果为准。
              </p>
            </div>
          </section>

          <section class="surface-card section-card">
            <div class="section-head">
              <div>
                <p class="section-eyebrow">Messages</p>
                <h2 class="section-title">协作沟通记录</h2>
              </div>
              <span class="soft-pill">{{ messages.length }} 条消息</span>
            </div>

            <div v-if="messages.length === 0" class="empty-block">
              暂无消息记录，后续沟通内容会持续保留在这里。
            </div>
            <div v-else class="msg-list">
              <div
                v-for="m in messages"
                :key="m.id"
                class="msg-item"
                :class="{ mine: m.senderId === currentUserId }"
              >
                <div class="msg-avatar">
                  {{ m.senderId === currentUserId ? '我' : (otherUser?.nickname || '对').slice(0, 1) }}
                </div>
                <div class="msg-bubble">
                  <div class="msg-bubble-top">
                    <span class="msg-sender">{{ m.senderId === currentUserId ? '我' : '对方' }}</span>
                    <span class="msg-time">{{ formatTime(m.createdAt) }}</span>
                  </div>
                  <p class="msg-content">{{ m.content }}</p>
                </div>
              </div>
            </div>

            <div v-if="canSendMessage" class="msg-send">
              <input
                v-model="msgText"
                class="msg-input"
                placeholder="输入本次约稿的沟通内容..."
                @keyup.enter="sendMsg"
              />
              <button class="msg-btn" @click="sendMsg" :disabled="!msgText.trim()">发送</button>
            </div>
          </section>
        </div>

        <aside class="side-col">
          <section class="surface-card side-card">
            <div class="mini-title-row">
              <h3>{{ isArtist ? '委托方信息' : '画师信息' }}</h3>
              <span>Collaborator</span>
            </div>
            <div class="user-row">
              <el-avatar :size="52" :src="otherUser?.avatarUrl || defaultAvatar">
                {{ (otherUser?.nickname || '?').charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <span class="user-name">{{ otherUser?.nickname || '平台用户' }}</span>
                <span class="user-role">{{ isArtist ? '当前由你负责执行创作' : '当前由对方负责创作交付' }}</span>
              </div>
            </div>
            <div class="identity-grid">
              <div class="identity-item">
                <span>委托编号</span>
                <strong>#{{ commission.id }}</strong>
              </div>
              <div class="identity-item">
                <span>创建时间</span>
                <strong>{{ formatDate(commission.createdAt) }}</strong>
              </div>
            </div>
          </section>

          <section class="surface-card side-card">
            <div class="mini-title-row">
              <h3>金额摘要</h3>
              <span>Finance</span>
            </div>
            <div class="amount-summary">
              <div v-if="commission.budget" class="amount-row">
                <span>预算参考</span>
                <strong class="amount-val">¥{{ commission.budget }}</strong>
              </div>
              <div v-if="commission.totalAmount" class="amount-row">
                <span>报价总额</span>
                <strong class="amount-val total">¥{{ commission.totalAmount }}</strong>
              </div>
              <div v-if="commission.depositAmount" class="amount-row">
                <span>定金</span>
                <strong class="amount-val">¥{{ commission.depositAmount }}</strong>
              </div>
              <div v-if="commission.totalAmount && commission.depositAmount" class="amount-row">
                <span>尾款</span>
                <strong class="amount-val">¥{{ (commission.totalAmount - commission.depositAmount).toFixed(2) }}</strong>
              </div>
            </div>
          </section>

          <section v-if="showActionPanel" class="surface-card side-card action-card">
            <div class="mini-title-row">
              <h3>当前可操作项</h3>
              <span>Actions</span>
            </div>
            <p class="action-tip">
              {{ isArtist ? '按流程推进报价、开工与交付。' : '根据当前阶段完成支付确认、验收或申请售后。' }}
            </p>
            <div class="action-list">
              <template v-if="!isArtist">
                <button v-if="commission.status === 'QUOTED'" class="action-btn primary" @click="payDeposit">
                  接受报价并支付定金
                </button>
                <button v-if="commission.status === 'DELIVERED'" class="action-btn primary" @click="payFinal">
                  确认收货并支付尾款
                </button>
                <button v-if="commission.status === 'DELIVERED'" class="action-btn outline" @click="doRevision">
                  请求修改
                </button>
                <button
                  v-if="afterSaleActionConfig"
                  class="action-btn outline"
                  :disabled="hasOpenAfterSale"
                  @click="handleAfterSaleRequest"
                >
                  {{ afterSaleActionConfig.buttonText }}
                </button>
              </template>

              <template v-if="isArtist">
                <button v-if="commission.status === 'PENDING'" class="action-btn primary" @click="showQuoteDialog = true">
                  提交报价
                </button>
                <button v-if="commission.status === 'DEPOSIT_PAID'" class="action-btn primary" @click="doStart">
                  开始创作
                </button>
                <button v-if="commission.status === 'IN_PROGRESS'" class="action-btn primary" @click="showDeliverDialog = true">
                  交付作品
                </button>
              </template>

              <button v-if="canCancel" class="action-btn danger" @click="doCancel">
                取消约稿
              </button>
            </div>
          </section>

          <section class="surface-card side-card">
            <div class="mini-title-row">
              <h3>流程进度</h3>
              <span>Timeline</span>
            </div>
            <div class="timeline">
              <div
                v-for="(step, index) in timeline"
                :key="index"
                class="timeline-step"
                :class="{ active: step.active, done: step.done }"
              >
                <div class="timeline-dot"></div>
                <div class="timeline-content">
                  <div class="timeline-label">{{ step.label }}</div>
                  <div class="timeline-caption">{{ step.active ? '当前所处阶段' : (step.done ? '已完成' : '待推进') }}</div>
                </div>
              </div>
            </div>
          </section>

          <section class="surface-card side-card stage-card">
            <div class="mini-title-row">
              <h3>阶段提示</h3>
              <span>Guide</span>
            </div>
            <p v-if="commission.status === 'PENDING'" class="stage-copy">当前需求已提交，等待画师给出报价和执行安排。</p>
            <p v-else-if="commission.status === 'QUOTED'" class="stage-copy">报价已经生成，适合在这里确认预算、周期和修改次数。</p>
            <p v-else-if="commission.status === 'DEPOSIT_PAID'" class="stage-copy">定金已完成支付，合作正式开始，建议尽快进入创作阶段。</p>
            <p v-else-if="commission.status === 'IN_PROGRESS'" class="stage-copy">项目正在进行中，建议通过沟通记录同步关键节点与反馈。</p>
            <p v-else-if="commission.status === 'DELIVERED'" class="stage-copy">作品已交付，请尽快验收、提出修改意见或完成尾款支付。</p>
            <p v-else-if="commission.status === 'COMPLETED'" class="stage-copy">这张约稿单已完成，所有支付与协作记录都会长期保留在本页。</p>
            <p v-else class="stage-copy">当前订单已结束或关闭，如有争议可根据规则联系平台处理。</p>
          </section>
        </aside>
      </div>

      <div v-if="showQuoteDialog" class="modal-overlay" @click.self="showQuoteDialog = false">
        <div class="modal-body">
          <div class="modal-header">
            <div>
              <p class="modal-kicker">Quote Setup</p>
              <h3>提交报价</h3>
            </div>
            <button class="modal-close" @click="showQuoteDialog = false">×</button>
          </div>
          <div class="modal-form">
            <label>报价金额 (¥)</label>
            <input v-model.number="quoteForm.totalAmount" type="number" class="form-input" />
            <label>定金比例</label>
            <input v-model.number="quoteForm.depositRatio" type="number" min="0.1" max="1" step="0.1" class="form-input" />
            <label>预计天数</label>
            <input v-model.number="quoteForm.estimatedDays" type="number" class="form-input" />
            <label>允许修改次数</label>
            <input v-model.number="quoteForm.revisionsAllowed" type="number" class="form-input" />
            <label>报价说明</label>
            <textarea v-model="quoteForm.quoteNote" rows="3" class="form-input"></textarea>
          </div>
          <div class="modal-actions">
            <button class="btn-cancel" @click="showQuoteDialog = false">取消</button>
            <button class="btn-submit" @click="doQuote">确认报价</button>
          </div>
        </div>
      </div>

      <div v-if="showDeliverDialog" class="modal-overlay" @click.self="showDeliverDialog = false">
        <div class="modal-body">
          <div class="modal-header">
            <div>
              <p class="modal-kicker">Delivery</p>
              <h3>交付作品</h3>
            </div>
            <button class="modal-close" @click="showDeliverDialog = false">×</button>
          </div>
          <div class="modal-form">
            <label>作品链接 <span class="danger-text">*</span></label>
            <input v-model="deliverUrl" class="form-input" placeholder="输入作品下载或查看链接" />
            <label>交付说明</label>
            <textarea
              v-model="deliverNote"
              rows="3"
              class="form-input"
              placeholder="补充交付内容、版本说明或文件备注"
            ></textarea>
          </div>
          <div class="modal-actions">
            <button class="btn-cancel" @click="showDeliverDialog = false">取消</button>
            <button class="btn-submit" @click="doDeliver">确认交付</button>
          </div>
        </div>
      </div>

      <div v-if="showCouponDialog" class="modal-overlay" @click.self="showCouponDialog = false">
        <div class="modal-body coupon-modal">
          <div class="modal-header">
            <div>
              <p class="modal-kicker">Coupon</p>
              <h3>选择优惠券</h3>
            </div>
            <button class="modal-close" @click="showCouponDialog = false">×</button>
          </div>

          <div v-if="couponLoading" class="empty-coupon">正在加载可用优惠券...</div>
          <div v-else>
            <div v-if="availableCoupons.length === 0" class="empty-coupon">
              当前没有可用优惠券，可直接继续支付。
            </div>
            <div v-else class="coupon-list">
              <div
                v-for="c in availableCoupons"
                :key="c.userCouponId"
                class="coupon-item"
                :class="{ selected: selectedCouponId === c.userCouponId }"
                @click="selectedCouponId = selectedCouponId === c.userCouponId ? null : c.userCouponId"
              >
                <div class="coupon-left">
                  <span class="coupon-discount">
                    <template v-if="c.type === 'FIXED'">¥{{ c.discountValue }}</template>
                    <template v-else>{{ c.discountValue }}%</template>
                  </span>
                  <span class="coupon-type">{{ c.type === 'FIXED' ? '满减券' : '折扣券' }}</span>
                </div>
                <div class="coupon-right">
                  <span class="coupon-name">{{ c.name }}</span>
                  <span class="coupon-save">可省 ¥{{ c.discountAmount }}</span>
                  <span v-if="c.minOrderAmount > 0" class="coupon-min">满 ¥{{ c.minOrderAmount }} 可用</span>
                  <span class="coupon-expire">{{ new Date(c.endTime).toLocaleDateString('zh-CN') }} 到期</span>
                </div>
                <div class="coupon-check">
                  <span v-if="selectedCouponId === c.userCouponId" class="check-icon">✓</span>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn-cancel" @click="showCouponDialog = false">取消</button>
            <button class="btn-submit" @click="confirmPayment">
              {{ selectedCouponId ? '使用优惠券并支付' : '直接支付' }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="showFeeDialog" class="modal-overlay" @click.self="showFeeDialog = false">
        <div class="modal-body fee-modal">
          <div class="modal-header">
            <div>
              <p class="modal-kicker">Payment Breakdown</p>
              <h3>费用明细</h3>
            </div>
            <button class="modal-close" @click="showFeeDialog = false">×</button>
          </div>

          <div class="fee-breakdown">
            <div class="fee-row">
              <span class="fee-label">{{ feeDetail.payLabel }}</span>
              <span class="fee-value">¥{{ feeDetail.originalAmount }}</span>
            </div>
            <div v-if="feeDetail.payLabel === '约稿定金'" class="fee-tip">
              定金支付不使用优惠券，优惠券仅在支付尾款时可参与抵扣。
            </div>
            <div v-if="feeDetail.couponDiscount > 0" class="fee-row">
              <span class="fee-label">优惠券抵扣</span>
              <span class="fee-value green">-¥{{ feeDetail.couponDiscount }}</span>
            </div>
            <div class="fee-row">
              <span class="fee-label">平台服务费 (5%)</span>
              <span class="fee-value">+¥{{ feeDetail.platformFee }}</span>
            </div>
            <div v-if="feeDetail.feeDiscount > 0" class="fee-row">
              <span class="fee-label">{{ feeDetail.vipLevel }} 手续费减免</span>
              <span class="fee-value green">-¥{{ feeDetail.feeDiscount }}</span>
            </div>
            <div class="fee-divider"></div>
            <div class="fee-row total">
              <span class="fee-label">实付金额</span>
              <span class="fee-value accent">¥{{ feeDetail.totalAmount }}</span>
            </div>
          </div>

          <div class="modal-actions">
            <button class="btn-cancel" @click="showFeeDialog = false">取消</button>
            <button class="btn-submit" :disabled="paying" @click="doFinalPayment">确认支付</button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="loading-wrap">
      <div class="loading-card">
        <p v-if="loadError">{{ loadError }}</p>
        <p v-else>正在加载约稿详情...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getCommission,
  quoteCommission,
  startWork,
  deliverWork,
  requestRevision,
  cancelCommission,
  getCommissionMessages,
  sendCommissionMessage
} from '@/api/commission'
import {
  createPayment,
  getCommissionPayments,
  continuePay,
  getAvailableCouponsForOrder
} from '@/api/payment'
import { getMyFeedbacks, submitAfterSale } from '@/api/feedback'
import { getMyMembership } from '@/api/membership'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const commissionId = Number(route.params.id)
const currentUserId = computed(() => userStore.user?.id)
const commission = ref(null)
const payments = ref([])
const messages = ref([])
const otherUser = ref(null)
const loadError = ref('')
const msgText = ref('')
const afterSales = ref([])
const afterSaleLoading = ref(false)

const OPEN_AFTER_SALE_STATUSES = ['PENDING', 'PROCESSING']
const afterSaleStatusLabelMap = {
  PENDING: '待审核',
  PROCESSING: '处理中',
  RESOLVED: '已处理',
  CLOSED: '已关闭'
}
const afterSaleResolutionLabelMap = {
  REFUND_EXECUTED: '已执行退款',
  REFUND_REJECTED: '未通过退款审核',
  INTERVENTION_CLOSED: '售后已关闭'
}
const afterSaleActionLabelMap = {
  PLATFORM_INTERVENTION: '平台介入',
  REFUND_REVIEW: '退款审核'
}

const showCouponDialog = ref(false)
const availableCoupons = ref([])
const selectedCouponId = ref(null)
const pendingPaymentType = ref(null)
const couponLoading = ref(false)

const showFeeDialog = ref(false)
const paying = ref(false)
const feeDetail = ref({
  payLabel: '',
  originalAmount: '0',
  couponDiscount: 0,
  platformFee: '0',
  feeDiscount: 0,
  vipLevel: '',
  totalAmount: '0'
})
const pendingPayCouponId = ref(null)

const showQuoteDialog = ref(false)
const showDeliverDialog = ref(false)
const deliverUrl = ref('')
const deliverNote = ref('')
const quoteForm = ref({
  totalAmount: null,
  depositRatio: 0.3,
  estimatedDays: 7,
  revisionsAllowed: 2,
  quoteNote: ''
})

const statusMap = {
  PENDING: '待报价',
  QUOTED: '已报价',
  DEPOSIT_PAID: '已付定金',
  IN_PROGRESS: '创作中',
  DELIVERED: '已交付',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REJECTED: '已拒绝'
}
const cancelRoleLabelMap = {
  CLIENT: '委托方取消',
  ARTIST: '画师取消',
  ADMIN: '管理员取消'
}
const paymentStatusMap = {
  PENDING: '待支付',
  PAID: '已支付',
  REFUNDED: '已退款',
  FAILED: '失败',
  CLOSED: '已关闭'
}

const isArtist = computed(() => commission.value && currentUserId.value === commission.value.artistId)
const hasQuote = computed(() => {
  const status = commission.value?.status
  return status && !['PENDING', 'CANCELLED', 'REJECTED'].includes(status)
})
const canCancel = computed(() => {
  const status = commission.value?.status
  return ['PENDING', 'QUOTED', 'DEPOSIT_PAID', 'IN_PROGRESS'].includes(status)
})
const canSendMessage = computed(() => {
  const status = commission.value?.status
  return status && !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(status)
})
const showActions = computed(() => {
  const status = commission.value?.status
  return status && !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(status)
})

const latestPaidPayment = computed(() => {
  return [...payments.value]
    .filter((item) => item.status === 'PAID')
    .sort((a, b) => new Date(b.paidAt || b.createdAt || 0) - new Date(a.paidAt || a.createdAt || 0))[0] || null
})

const latestFinalPayment = computed(() => {
  return [...payments.value]
    .filter((item) => item.status === 'PAID' && item.paymentType === 'FINAL_PAYMENT')
    .sort((a, b) => new Date(b.paidAt || b.createdAt || 0) - new Date(a.paidAt || a.createdAt || 0))[0] || null
})

const hasOpenAfterSale = computed(() => {
  return afterSales.value.some((item) => OPEN_AFTER_SALE_STATUSES.includes(item.status))
})

const afterSaleActionConfig = computed(() => {
  if (isArtist.value || !commission.value) {
    return null
  }

  const status = commission.value.status
  if (['DEPOSIT_PAID', 'IN_PROGRESS', 'DELIVERED'].includes(status) && latestPaidPayment.value) {
    return {
      requestedAction: 'PLATFORM_INTERVENTION',
      buttonText: hasOpenAfterSale.value ? '售后处理中' : '申请平台介入',
      dialogTitle: '申请平台介入',
      title: `约稿平台介入申请 #${commission.value.id}`,
      payment: latestPaidPayment.value,
      notice: [
        '当前阶段更适合先申请平台介入，而不是直接退款。',
        '请说明画师违约、交付争议或沟通卡点，并尽量补充证据线索。',
        '平台审核后会决定是否继续协调，或进入取消约稿与退款处理。'
      ].join('\n')
    }
  }

  if (status === 'COMPLETED' && latestFinalPayment.value) {
    return {
      requestedAction: 'REFUND_REVIEW',
      buttonText: hasOpenAfterSale.value ? '售后处理中' : '申请售后/申诉',
      dialogTitle: '申请售后/申诉',
      title: `约稿售后申请 #${commission.value.id} - 尾款审核`,
      payment: latestFinalPayment.value,
      notice: [
        '尾款支付通常代表你已经确认交付结果。',
        '如存在抄袭、欺诈、严重不符或其他重大违约情形，可在此提交售后申诉。',
        '平台不会自动退款，需要管理员审核后再决定具体处理方式。'
      ].join('\n')
    }
  }

  return null
})

const showActionPanel = computed(() => {
  return !!afterSaleActionConfig.value || showActions.value
})

const refUrlList = computed(() => {
  if (!commission.value?.referenceUrls) return []
  return commission.value.referenceUrls.split('\n').filter((url) => url.trim())
})

const timeline = computed(() => {
  const steps = [
    { key: 'PENDING', label: '提交需求' },
    { key: 'QUOTED', label: '画师报价' },
    { key: 'DEPOSIT_PAID', label: '支付定金' },
    { key: 'IN_PROGRESS', label: '创作中' },
    { key: 'DELIVERED', label: '作品交付' },
    { key: 'COMPLETED', label: '完成' }
  ]
  const order = ['PENDING', 'QUOTED', 'DEPOSIT_PAID', 'IN_PROGRESS', 'DELIVERED', 'COMPLETED']
  const index = order.indexOf(commission.value?.status)
  return steps.map((step, stepIndex) => ({
    ...step,
    done: stepIndex < index,
    active: stepIndex === index
  }))
})

onMounted(async () => {
  await loadData()
})

async function loadData() {
  try {
    const res = await getCommission(commissionId)
    if (res.code === 200) {
      commission.value = res.data
      quoteForm.value.totalAmount = res.data.budget || null

      const otherId = isArtist.value ? res.data.clientId : res.data.artistId
      if (otherId) {
        try {
          const userRes = await request({ url: `/api/users/${otherId}`, method: 'get' })
          if (userRes.code === 200) {
            otherUser.value = userRes.data
          }
        } catch {}
      }
    } else {
      loadError.value = res.message || '加载失败'
    }
  } catch {
    loadError.value = '加载约稿详情失败'
  }

  try {
    const paymentRes = await getCommissionPayments(commissionId)
    if (paymentRes.code === 200) {
      payments.value = paymentRes.data || []
    }
  } catch {}

  try {
    const messageRes = await getCommissionMessages(commissionId)
    if (messageRes.code === 200) {
      messages.value = messageRes.data || []
    }
  } catch {}

  await loadAfterSales()
}

async function loadAfterSales() {
  if (!commission.value) {
    afterSales.value = []
    return
  }

  afterSaleLoading.value = true
  try {
    const res = await getMyFeedbacks({ page: 0, size: 20, type: 'AFTER_SALE', commissionId })
    afterSales.value = res.code === 200 ? (res.data?.records || []) : []
  } catch {
    afterSales.value = []
  } finally {
    afterSaleLoading.value = false
  }
}

function getAfterSaleStatusLabel(item) {
  return afterSaleStatusLabelMap[item?.status] || item?.status || ''
}

function getAfterSaleResolutionLabel(item) {
  return afterSaleResolutionLabelMap[item?.resolution] || ''
}

function getAfterSaleActionLabel(item) {
  return afterSaleActionLabelMap[item?.requestedAction] || '售后申请'
}

function getAfterSaleSummary(item) {
  const resolution = getAfterSaleResolutionLabel(item)
  return resolution || getAfterSaleStatusLabel(item)
}

function formatPaymentType(type) {
  if (type === 'DEPOSIT') return '定金'
  if (type === 'FINAL_PAYMENT') return '尾款'
  return type || '支付记录'
}

function formatTime(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('zh-CN')
}

function formatDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleDateString('zh-CN')
}

async function handleAfterSaleRequest() {
  if (!afterSaleActionConfig.value) {
    ElMessage.info('当前阶段暂不支持发起售后申请')
    return
  }

  if (hasOpenAfterSale.value) {
    ElMessage.info('当前约稿已有处理中售后，请等待管理员审核')
    return
  }

  const config = afterSaleActionConfig.value

  try {
    const { value } = await ElMessageBox.prompt(
      `${config.notice}\n\n请尽量详细说明问题经过、证据线索和你的诉求：`,
      config.dialogTitle,
      {
        confirmButtonText: '提交申请',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '至少填写 10 个字，便于平台判断事实经过',
        inputValidator: (input) => input && input.trim().length >= 10 ? true : '请至少填写 10 个字的申请说明'
      }
    )

    const res = await submitAfterSale({
      commissionId,
      paymentId: config.payment?.id || null,
      requestedAction: config.requestedAction,
      title: config.title,
      content: value.trim(),
      contactInfo: userStore.user?.email || userStore.user?.phone || userStore.user?.username || ''
    })

    if (res.code === 200) {
      ElMessage.success('售后申请已提交，平台审核后会通过站内通知反馈结果')
      await loadAfterSales()
    } else {
      ElMessage.error(res.message || '提交售后申请失败')
    }
  } catch {}
}

async function payDeposit() {
  await openCouponSelector('DEPOSIT')
}

async function payFinal() {
  await openCouponSelector('FINAL_PAYMENT')
}

async function openCouponSelector(paymentType) {
  pendingPaymentType.value = paymentType
  selectedCouponId.value = null

  if (paymentType === 'DEPOSIT') {
    availableCoupons.value = []
    await confirmPayment()
    return
  }

  couponLoading.value = true
  showCouponDialog.value = true

  const currentCommission = commission.value
  let orderAmount = 0
  if (paymentType === 'DEPOSIT') {
    orderAmount = currentCommission.depositAmount || 0
  } else {
    orderAmount = (currentCommission.totalAmount || 0) - (currentCommission.depositAmount || 0)
  }

  try {
    const res = await getAvailableCouponsForOrder(orderAmount)
    availableCoupons.value = res.code === 200 ? (res.data || []) : []
  } catch {
    availableCoupons.value = []
  } finally {
    couponLoading.value = false
  }
}

async function confirmPayment() {
  showCouponDialog.value = false

  const currentCommission = commission.value
  const type = pendingPaymentType.value
  const originalAmount = parseFloat(
    type === 'DEPOSIT'
      ? currentCommission.depositAmount
      : (currentCommission.totalAmount - currentCommission.depositAmount).toFixed(2)
  )

  let couponDiscount = 0
  if (selectedCouponId.value) {
    const coupon = availableCoupons.value.find((item) => item.userCouponId === selectedCouponId.value)
    if (coupon) {
      couponDiscount = parseFloat(coupon.discountAmount) || 0
    }
  }
  const afterCoupon = Math.max(0, originalAmount - couponDiscount)
  const platformFee = parseFloat((afterCoupon * 0.05).toFixed(2))

  let feeDiscountAmount = 0
  let vipLevel = ''
  try {
    const membershipRes = await getMyMembership()
    if (membershipRes.code === 200 && membershipRes.data && !membershipRes.data.expired) {
      vipLevel = membershipRes.data.level || ''
      if (vipLevel === 'SVIP') {
        feeDiscountAmount = parseFloat((platformFee * 0.1).toFixed(2))
      } else if (vipLevel === 'VIP') {
        feeDiscountAmount = parseFloat((platformFee * 0.05).toFixed(2))
      }
    }
  } catch {}

  const actualFee = parseFloat((platformFee - feeDiscountAmount).toFixed(2))
  const totalAmount = parseFloat((afterCoupon + actualFee).toFixed(2))

  feeDetail.value = {
    payLabel: type === 'DEPOSIT' ? '约稿定金' : '约稿尾款',
    originalAmount: originalAmount.toFixed(2),
    couponDiscount,
    platformFee: platformFee.toFixed(2),
    feeDiscount: feeDiscountAmount,
    vipLevel,
    totalAmount: totalAmount.toFixed(2)
  }
  pendingPayCouponId.value = selectedCouponId.value
  showFeeDialog.value = true
}

async function doFinalPayment() {
  paying.value = true
  try {
    const data = {
      commissionId,
      paymentType: pendingPaymentType.value
    }
    if (pendingPayCouponId.value) {
      data.userCouponId = pendingPayCouponId.value
    }
    const res = await createPayment(data)
    if (res.code === 200 && res.data) {
      showFeeDialog.value = false
      submitAlipayForm(res.data)
    } else {
      ElMessage.error(res.message || '创建支付失败')
    }
  } catch {
    ElMessage.error('发起支付失败')
  } finally {
    paying.value = false
  }
}

function submitAlipayForm(htmlForm) {
  const container = document.createElement('div')
  container.innerHTML = htmlForm
  document.body.appendChild(container)
  const form = container.querySelector('form')
  if (form) {
    form.submit()
  }
}

function isOrderExpired(payment) {
  if (!payment.expireAt) return false
  return new Date(payment.expireAt) < new Date()
}

function formatCountdown(expireAt) {
  const diff = new Date(expireAt) - new Date()
  if (diff <= 0) return '已超时'
  const mins = Math.floor(diff / 60000)
  const secs = Math.floor((diff % 60000) / 1000)
  return `剩余 ${mins}分${secs}秒`
}

async function handleContinuePay(orderNo) {
  try {
    const res = await continuePay(orderNo)
    if (res.code === 200 && res.data) {
      submitAlipayForm(res.data)
    } else {
      ElMessage.error(res.message || '继续支付失败')
      await loadData()
    }
  } catch {
    ElMessage.error('继续支付失败')
    await loadData()
  }
}

async function doQuote() {
  if (!quoteForm.value.totalAmount || quoteForm.value.totalAmount <= 0) {
    ElMessage.warning('请输入报价金额')
    return
  }

  try {
    const res = await quoteCommission(commissionId, quoteForm.value)
    if (res.code === 200) {
      ElMessage.success('报价已发送')
      showQuoteDialog.value = false
      await loadData()
    } else {
      ElMessage.error(res.message || '报价失败')
    }
  } catch {
    ElMessage.error('报价失败')
  }
}

async function doStart() {
  try {
    const res = await startWork(commissionId)
    if (res.code === 200) {
      ElMessage.success('已开始创作')
      await loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

async function doDeliver() {
  if (!deliverUrl.value.trim()) {
    ElMessage.warning('请输入作品链接')
    return
  }

  try {
    const res = await deliverWork(commissionId, {
      deliveryUrl: deliverUrl.value,
      deliveryNote: deliverNote.value
    })
    if (res.code === 200) {
      ElMessage.success('已交付作品')
      showDeliverDialog.value = false
      await loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

async function doRevision() {
  try {
    await ElMessageBox.confirm('确认请求修改吗？', '提示')
    const res = await requestRevision(commissionId)
    if (res.code === 200) {
      ElMessage.success('已请求修改')
      await loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {}
}

async function doCancel() {
  const status = commission.value?.status
  const hasPaid = ['DEPOSIT_PAID', 'IN_PROGRESS', 'DELIVERED'].includes(status)

  let warningMsg = ''
  if (isArtist.value) {
    if (hasPaid) {
      warningMsg =
        '<div style="margin-bottom:12px;padding:12px;background:#fef0e7;border-radius:8px;border-left:4px solid #e6a23c;">' +
        '<p style="margin:0 0 6px;font-weight:600;color:#e6a23c;">退款提示</p>' +
        '<p style="margin:0;color:#606266;font-size:13px;">画师主动取消约稿时，委托方已支付的<strong>所有款项（包括定金）</strong>将<strong>全额退回</strong>。</p>' +
        '</div>'
    }
    warningMsg += '<p style="margin:8px 0 0;color:#909399;font-size:13px;">请输入取消原因：</p>'
  } else if (hasPaid) {
    warningMsg =
      '<div style="margin-bottom:12px;padding:12px;background:#fef0e7;border-radius:8px;border-left:4px solid #f56c6c;">' +
      '<p style="margin:0 0 6px;font-weight:600;color:#f56c6c;">定金不予退还</p>' +
      '<p style="margin:0;color:#606266;font-size:13px;">委托方主动取消约稿时，<strong>定金通常不予退还</strong>。</p>' +
      '<p style="margin:4px 0 0;color:#606266;font-size:13px;">如有已支付尾款，尾款部分会进入退款流程。</p>' +
      '</div>' +
      '<div style="margin-bottom:12px;padding:10px;background:#f0f9ff;border-radius:8px;font-size:12px;color:#909399;">' +
      '如果取消原因在画师侧，建议先与画师沟通由画师发起取消，以便按规则处理退款。' +
      '</div>' +
      '<p style="margin:8px 0 0;color:#909399;font-size:13px;">请输入取消原因：</p>'
  } else {
    warningMsg = '<p style="margin:0;color:#909399;font-size:13px;">当前尚未支付，取消不涉及退款。请输入取消原因：</p>'
  }

  try {
    const { value: reason } = await ElMessageBox.prompt('', '取消约稿', {
      message: warningMsg,
      dangerouslyUseHTMLString: true,
      inputPlaceholder: '取消原因（选填）',
      confirmButtonText: hasPaid ? '确认取消并处理退款' : '确认取消',
      cancelButtonText: '我再想想',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
      customClass: 'cancel-commission-dialog'
    })

    const res = await cancelCommission(commissionId, reason || '')
    if (res.code === 200) {
      if (isArtist.value && hasPaid) {
        ElMessage.success('约稿已取消，款项将退还给委托方')
      } else if (!isArtist.value && hasPaid) {
        ElMessage.success('约稿已取消，定金不退，尾款如有支付将进入退款流程')
      } else {
        ElMessage.success('约稿已取消')
      }
      await loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {}
}

async function sendMsg() {
  if (!msgText.value.trim()) return

  try {
    const res = await sendCommissionMessage(commissionId, {
      content: msgText.value.trim()
    })
    if (res.code === 200) {
      msgText.value = ''
      await loadData()
    }
  } catch {
    ElMessage.error('发送失败，请稍后重试')
  }
}
</script>

<style scoped>
.commission-detail-page {
  --page-bg: #f5f7fb;
  --ink-strong: #172033;
  --ink-main: #324055;
  --ink-soft: #6d7b92;
  --line-soft: rgba(136, 154, 181, 0.2);
  --surface: rgba(255, 255, 255, 0.82);
  --accent: #0f92ff;
  --accent-deep: #0666d3;
  --accent-soft: rgba(15, 146, 255, 0.12);
  --success: #25a96c;
  --warning: #d98a1f;
  --danger: #e25858;
  --shadow-main: 0 24px 60px rgba(20, 33, 61, 0.08);
  min-height: calc(100vh - 64px);
  position: relative;
  overflow: hidden;
  padding: 28px 22px 48px;
  background:
    radial-gradient(circle at top left, rgba(15, 146, 255, 0.14), transparent 26%),
    radial-gradient(circle at top right, rgba(255, 132, 92, 0.16), transparent 24%),
    linear-gradient(180deg, #fbfdff 0%, var(--page-bg) 100%);
}

.page-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(42px);
  opacity: 0.9;
  pointer-events: none;
}

.glow-left {
  width: 280px;
  height: 280px;
  top: -80px;
  left: -60px;
  background: rgba(15, 146, 255, 0.16);
}

.glow-right {
  width: 320px;
  height: 320px;
  right: -100px;
  top: 220px;
  background: rgba(255, 148, 94, 0.14);
}

.page-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(15, 23, 42, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 23, 42, 0.025) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.45), transparent 70%);
  pointer-events: none;
}

.page-container {
  position: relative;
  z-index: 1;
  max-width: 1240px;
  margin: 0 auto;
}

.surface-card {
  border: 1px solid rgba(255, 255, 255, 0.88);
  background: var(--surface);
  box-shadow: var(--shadow-main);
  backdrop-filter: blur(16px);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: 1px solid rgba(143, 160, 189, 0.24);
  background: rgba(255, 255, 255, 0.78);
  border-radius: 999px;
  color: var(--ink-main);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 18px;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.back-btn:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 12px 28px rgba(20, 33, 61, 0.08);
}

.hero-panel {
  border-radius: 30px;
  padding: 30px;
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.95fr);
  gap: 24px;
  margin-bottom: 18px;
}

.hero-kicker,
.section-eyebrow,
.modal-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #5f7ea6;
}

.kicker-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0f92ff, #4dbbff);
  box-shadow: 0 0 0 6px rgba(15, 146, 255, 0.12);
}

.hero-title-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
  margin: 12px 0 14px;
}

.hero-title-row h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.1;
  letter-spacing: -0.04em;
  color: var(--ink-strong);
}

.hero-desc {
  max-width: 680px;
  margin: 0 0 18px;
  font-size: 15px;
  line-height: 1.85;
  color: var(--ink-soft);
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-meta-item,
.soft-pill {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--line-soft);
  background: rgba(255, 255, 255, 0.72);
  font-size: 12px;
  color: var(--ink-soft);
}

.hero-stats {
  display: grid;
  gap: 12px;
}

.hero-stat-card {
  padding: 18px 18px 16px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(244, 248, 255, 0.88));
  border: 1px solid rgba(207, 220, 237, 0.86);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85);
}

.hero-stat-label,
.metric-label,
.insight-chip-label,
.amount-row span,
.identity-item span,
.timeline-caption,
.payment-meta,
.coupon-type,
.coupon-min,
.coupon-expire,
.user-role {
  color: var(--ink-soft);
}

.hero-stat-label,
.metric-label,
.insight-chip-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.03em;
}

.hero-stat-value {
  display: block;
  margin-top: 8px;
  color: var(--ink-strong);
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.hero-stat-note {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--ink-soft);
}

.insight-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.insight-chip {
  padding: 18px 20px;
  border-radius: 20px;
  border: 1px solid rgba(219, 228, 242, 0.9);
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 14px 36px rgba(20, 33, 61, 0.05);
}

.insight-chip strong {
  display: block;
  margin-top: 6px;
  color: var(--ink-strong);
  font-size: 16px;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.22fr) 340px;
  gap: 18px;
  align-items: start;
}

.main-col,
.side-col {
  min-width: 0;
}

.side-col {
  position: sticky;
  top: 92px;
}

.section-card,
.side-card {
  border-radius: 28px;
  padding: 24px;
  margin-bottom: 16px;
}

.section-head,
.mini-title-row,
.payment-top,
.after-sale-top,
.msg-bubble-top,
.modal-header,
.modal-actions,
.fee-row,
.amount-row,
.identity-grid,
.payment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-head {
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.section-title,
.mini-title-row h3,
.modal-header h3 {
  margin: 0;
  color: var(--ink-strong);
}

.section-title {
  font-size: 22px;
  letter-spacing: -0.03em;
}

.mini-title-row {
  margin-bottom: 16px;
  gap: 12px;
}

.mini-title-row h3 {
  font-size: 18px;
}

.mini-title-row span {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #7c8daa;
}

.section-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.accent-pill {
  color: var(--accent-deep);
  background: var(--accent-soft);
  border-color: rgba(15, 146, 255, 0.18);
}

.desc-text,
.note-panel p,
.after-sale-content,
.cancel-reason,
.stage-copy {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.9;
  color: var(--ink-main);
}

.reference-panel {
  margin-top: 22px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(180deg, rgba(244, 248, 255, 0.95), rgba(255, 255, 255, 0.92));
  border: 1px solid rgba(214, 225, 241, 0.88);
}

.reference-list {
  display: grid;
  gap: 10px;
  margin-top: 14px;
}

.reference-link {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(218, 229, 244, 0.9);
  color: var(--ink-main);
  text-decoration: none;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.reference-link:hover {
  transform: translateY(-1px);
  border-color: rgba(15, 146, 255, 0.3);
  box-shadow: 0 14px 28px rgba(15, 146, 255, 0.08);
}

.reference-index {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(15, 146, 255, 0.12), rgba(77, 187, 255, 0.2));
  font-size: 12px;
  font-weight: 800;
  color: var(--accent-deep);
}

.reference-url {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quote-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.metric-card {
  padding: 18px;
  border-radius: 22px;
  background: rgba(247, 250, 255, 0.95);
  border: 1px solid rgba(218, 227, 241, 0.88);
}

.metric-value {
  display: block;
  margin-top: 8px;
  color: var(--ink-strong);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.metric-value.price {
  color: var(--accent-deep);
}

.note-panel {
  margin-top: 16px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, rgba(20, 33, 61, 0.04), rgba(15, 146, 255, 0.08));
  border: 1px solid rgba(216, 227, 242, 0.84);
}

.note-badge {
  display: inline-flex;
  margin-bottom: 10px;
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(20, 33, 61, 0.06);
  color: var(--ink-main);
  font-size: 12px;
  font-weight: 700;
}

.payment-list,
.after-sale-list {
  display: grid;
  gap: 12px;
}

.payment-item,
.after-sale-item {
  padding: 18px;
  border-radius: 22px;
  background: rgba(248, 250, 254, 0.96);
  border: 1px solid rgba(219, 228, 242, 0.92);
}

.payment-top,
.after-sale-top {
  gap: 16px;
  align-items: flex-start;
}

.payment-primary {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.payment-type {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(15, 146, 255, 0.1);
  color: var(--accent-deep);
  font-size: 12px;
  font-weight: 700;
}

.payment-amount {
  color: var(--ink-strong);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.payment-status,
.after-sale-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(20, 33, 61, 0.06);
  font-size: 12px;
  font-weight: 700;
}

.payment-meta,
.payment-actions,
.after-sale-meta {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
  font-size: 12px;
  line-height: 1.7;
}

.payment-meta span,
.after-sale-meta span {
  display: inline-flex;
  align-items: center;
}

.tiny-action-btn {
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--accent), #4cb8ff);
  color: #fff;
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.tiny-action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(15, 146, 255, 0.22);
}

.expire-hint {
  color: var(--warning);
  font-weight: 600;
}

.payment-coupon-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 9px;
  border-radius: 999px;
  background: rgba(226, 88, 88, 0.08);
  color: var(--danger);
  font-size: 11px;
  font-weight: 700;
}

.ps-PAID {
  color: var(--success);
  background: rgba(37, 169, 108, 0.11);
}

.ps-PENDING {
  color: var(--warning);
  background: rgba(217, 138, 31, 0.12);
}

.ps-REFUNDED {
  color: var(--danger);
  background: rgba(226, 88, 88, 0.1);
}

.ps-CLOSED,
.refund-admin {
  color: var(--ink-soft);
}

.refund-time,
.refund-reason {
  color: var(--danger);
}

.after-sale-top h3 {
  margin: 0;
  font-size: 17px;
  color: var(--ink-strong);
}

.after-sale-top p {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--ink-soft);
}

.after-sale-content {
  margin-top: 12px;
}

.cancel-info-card {
  border: 1px solid rgba(255, 196, 157, 0.72);
  background: linear-gradient(180deg, rgba(255, 251, 246, 0.95), rgba(255, 255, 255, 0.92));
}

.cancel-role-badge {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.role-CLIENT {
  background: rgba(217, 138, 31, 0.12);
  color: var(--warning);
}

.role-ARTIST {
  background: rgba(15, 146, 255, 0.11);
  color: var(--accent-deep);
}

.role-ADMIN {
  background: rgba(50, 64, 85, 0.08);
  color: var(--ink-main);
}

.cancel-refund-hint {
  margin-top: 14px;
}

.cancel-refund-hint p {
  margin: 0;
  padding: 14px 16px;
  border-radius: 18px;
  font-size: 13px;
  line-height: 1.75;
}

.refund-ok {
  background: rgba(37, 169, 108, 0.08);
  color: var(--success);
}

.refund-partial {
  background: rgba(217, 138, 31, 0.08);
  color: var(--warning);
}

.msg-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 440px;
  overflow-y: auto;
  padding-right: 4px;
}

.msg-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.msg-item.mine {
  justify-content: flex-end;
}

.msg-avatar {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(15, 146, 255, 0.12), rgba(77, 187, 255, 0.24));
  color: var(--accent-deep);
  font-size: 13px;
  font-weight: 800;
  flex-shrink: 0;
}

.msg-item.mine .msg-avatar {
  order: 2;
  background: linear-gradient(135deg, rgba(255, 148, 94, 0.16), rgba(255, 199, 118, 0.26));
  color: #bd5b1d;
}

.msg-bubble {
  max-width: min(78%, 620px);
  padding: 14px 16px;
  border-radius: 22px;
  background: rgba(248, 250, 254, 0.98);
  border: 1px solid rgba(219, 228, 242, 0.92);
}

.msg-item.mine .msg-bubble {
  background: linear-gradient(135deg, rgba(15, 146, 255, 0.1), rgba(240, 248, 255, 0.95));
}

.msg-bubble-top {
  gap: 12px;
  margin-bottom: 8px;
}

.msg-sender {
  font-size: 13px;
  font-weight: 800;
  color: var(--ink-strong);
}

.msg-time {
  font-size: 11px;
  color: var(--ink-soft);
}

.msg-content {
  margin: 0;
  color: var(--ink-main);
  line-height: 1.8;
  word-break: break-word;
}

.msg-send {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  margin-top: 18px;
}

.msg-input,
.form-input {
  width: 100%;
  box-sizing: border-box;
  font-family: inherit;
  font-size: 14px;
  color: var(--ink-main);
  border: 1px solid rgba(204, 216, 235, 0.92);
  background: rgba(247, 250, 255, 0.94);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.msg-input {
  padding: 14px 16px;
  border-radius: 18px;
}

.msg-input:focus,
.form-input:focus {
  outline: none;
  border-color: rgba(15, 146, 255, 0.45);
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 0 0 4px rgba(15, 146, 255, 0.11);
}

.msg-btn,
.action-btn,
.btn-submit {
  border: none;
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease, background 0.18s ease;
}

.msg-btn {
  padding: 0 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--accent), #4cb8ff);
  color: #fff;
}

.msg-btn:hover,
.btn-submit:hover,
.action-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(15, 146, 255, 0.24);
}

.msg-btn:disabled,
.btn-submit:disabled,
.action-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  color: var(--ink-strong);
  font-size: 17px;
  font-weight: 800;
}

.user-role {
  font-size: 13px;
  line-height: 1.6;
}

.identity-grid {
  margin-top: 18px;
  gap: 12px;
}

.identity-item {
  flex: 1;
  min-width: 0;
  padding: 14px;
  border-radius: 18px;
  background: rgba(247, 250, 255, 0.95);
  border: 1px solid rgba(220, 229, 242, 0.88);
}

.identity-item strong {
  display: block;
  margin-top: 6px;
  color: var(--ink-strong);
  font-size: 15px;
}

.amount-summary {
  display: grid;
  gap: 12px;
}

.amount-row {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(247, 250, 255, 0.95);
  border: 1px solid rgba(220, 229, 242, 0.88);
  gap: 16px;
}

.amount-val {
  color: var(--ink-strong);
  font-size: 16px;
  font-weight: 800;
}

.amount-val.total {
  color: var(--accent-deep);
  font-size: 18px;
}

.action-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(244, 248, 255, 0.9));
}

.action-tip {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.75;
  color: var(--ink-soft);
}

.action-list {
  display: grid;
  gap: 10px;
}

.action-btn {
  width: 100%;
  min-height: 46px;
  padding: 0 18px;
  border-radius: 16px;
  font-size: 14px;
}

.action-btn.primary,
.btn-submit {
  background: linear-gradient(135deg, var(--accent), #4cb8ff);
  color: #fff;
}

.action-btn.outline {
  background: rgba(255, 255, 255, 0.88);
  color: var(--accent-deep);
  border: 1px solid rgba(15, 146, 255, 0.24);
}

.action-btn.outline:hover {
  transform: translateY(-1px);
  background: rgba(240, 247, 255, 0.98);
}

.action-btn.danger {
  background: rgba(255, 255, 255, 0.9);
  color: var(--danger);
  border: 1px solid rgba(226, 88, 88, 0.24);
}

.timeline {
  display: grid;
  gap: 2px;
}

.timeline-step {
  position: relative;
  display: grid;
  grid-template-columns: 16px minmax(0, 1fr);
  gap: 12px;
  padding: 10px 0;
}

.timeline-step:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 7px;
  top: 28px;
  width: 2px;
  height: calc(100% - 8px);
  background: rgba(193, 207, 227, 0.8);
}

.timeline-step.done:not(:last-child)::after {
  background: rgba(15, 146, 255, 0.38);
}

.timeline-dot {
  width: 16px;
  height: 16px;
  margin-top: 3px;
  border-radius: 50%;
  border: 2px solid rgba(193, 207, 227, 0.95);
  background: rgba(255, 255, 255, 0.98);
  position: relative;
  z-index: 1;
}

.timeline-step.done .timeline-dot {
  border-color: rgba(15, 146, 255, 0.52);
  background: rgba(15, 146, 255, 0.62);
}

.timeline-step.active .timeline-dot {
  border-color: var(--accent);
  background: var(--accent);
  box-shadow: 0 0 0 6px rgba(15, 146, 255, 0.12);
}

.timeline-label {
  color: var(--ink-strong);
  font-size: 14px;
  font-weight: 700;
}

.timeline-content {
  min-width: 0;
}

.timeline-step.active .timeline-label {
  color: var(--accent-deep);
}

.timeline-step.done .timeline-label {
  color: #226c9c;
}

.timeline-caption {
  margin-top: 4px;
  font-size: 12px;
}

.stage-card {
  background: linear-gradient(135deg, rgba(20, 33, 61, 0.96), rgba(33, 85, 143, 0.92));
  color: #fff;
  border: none;
  box-shadow: 0 24px 50px rgba(24, 45, 80, 0.22);
}

.stage-card .mini-title-row h3,
.stage-card .mini-title-row span,
.stage-copy {
  color: #fff;
}

.stage-card .mini-title-row span {
  color: rgba(255, 255, 255, 0.72);
}

.stage-copy {
  opacity: 0.9;
}

.empty-block,
.empty-coupon,
.loading-card {
  padding: 28px 20px;
  border-radius: 22px;
  border: 1px dashed rgba(186, 199, 220, 0.8);
  background: rgba(248, 251, 255, 0.86);
  text-align: center;
  color: var(--ink-soft);
  line-height: 1.8;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 360px;
}

.loading-card {
  min-width: min(460px, calc(100vw - 32px));
}

.status-badge {
  display: inline-flex;
  align-items: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.status-PENDING {
  background: rgba(217, 138, 31, 0.12);
  color: var(--warning);
}

.status-QUOTED {
  background: rgba(15, 146, 255, 0.11);
  color: var(--accent-deep);
}

.status-DEPOSIT_PAID {
  background: rgba(92, 108, 255, 0.12);
  color: #4450d7;
}

.status-IN_PROGRESS {
  background: rgba(255, 148, 94, 0.14);
  color: #c85c1f;
}

.status-DELIVERED {
  background: rgba(37, 169, 108, 0.11);
  color: var(--success);
}

.status-COMPLETED {
  background: rgba(28, 179, 123, 0.14);
  color: #1b915f;
}

.status-CANCELLED,
.status-REJECTED {
  background: rgba(74, 85, 104, 0.08);
  color: var(--ink-soft);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(10, 18, 30, 0.45);
  backdrop-filter: blur(8px);
}

.modal-body {
  width: min(100%, 540px);
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  border-radius: 28px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 28px 70px rgba(10, 18, 30, 0.22);
}

.modal-header {
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.modal-header h3 {
  margin-top: 6px;
  font-size: 24px;
  letter-spacing: -0.03em;
}

.modal-close {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(20, 33, 61, 0.06);
  color: var(--ink-main);
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
}

.modal-form {
  display: grid;
  gap: 10px;
}

.modal-form label {
  color: var(--ink-main);
  font-size: 13px;
  font-weight: 700;
}

.form-input {
  padding: 12px 14px;
  border-radius: 16px;
}

.danger-text {
  color: var(--danger);
}

.modal-actions {
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel,
.btn-submit {
  min-width: 104px;
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  font-size: 14px;
}

.btn-cancel {
  border: 1px solid rgba(204, 216, 235, 0.92);
  background: rgba(255, 255, 255, 0.92);
  color: var(--ink-main);
  cursor: pointer;
}

.btn-cancel:hover {
  background: rgba(245, 248, 252, 0.98);
}

.coupon-modal {
  width: min(100%, 620px);
}

.coupon-list {
  display: grid;
  gap: 12px;
}

.coupon-item {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr) 30px;
  gap: 14px;
  align-items: center;
  padding: 16px;
  border-radius: 22px;
  background: rgba(248, 250, 254, 0.98);
  border: 1px solid rgba(219, 228, 242, 0.92);
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.coupon-item:hover,
.coupon-item.selected {
  transform: translateY(-1px);
  border-color: rgba(15, 146, 255, 0.3);
  box-shadow: 0 14px 28px rgba(15, 146, 255, 0.08);
}

.coupon-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 88px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 148, 94, 0.14), rgba(255, 213, 157, 0.28));
}

.coupon-discount {
  color: #cb4f27;
  font-size: 24px;
  font-weight: 800;
}

.coupon-right {
  display: grid;
  gap: 4px;
}

.coupon-name {
  color: var(--ink-strong);
  font-size: 15px;
  font-weight: 800;
}

.coupon-save {
  color: var(--danger);
  font-size: 13px;
  font-weight: 700;
}

.coupon-check {
  display: flex;
  justify-content: center;
}

.check-icon {
  display: inline-grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(15, 146, 255, 0.12);
  color: var(--accent-deep);
  font-size: 14px;
  font-weight: 800;
}

.fee-modal {
  width: min(100%, 500px);
}

.fee-breakdown {
  padding: 4px 0;
}

.fee-row {
  padding: 10px 0;
}

.fee-label {
  color: var(--ink-main);
  font-size: 14px;
}

.fee-value {
  color: var(--ink-strong);
  font-size: 15px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.fee-value.green {
  color: var(--success);
}

.fee-value.accent {
  color: var(--accent-deep);
  font-size: 24px;
  font-weight: 800;
}

.fee-divider {
  border-top: 1px dashed rgba(186, 199, 220, 0.9);
  margin: 8px 0;
}

.fee-tip {
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 196, 157, 0.2);
  color: #a35a1e;
  font-size: 12px;
  line-height: 1.75;
}

.fee-row.total .fee-label {
  font-size: 15px;
  font-weight: 800;
}

@media (max-width: 1100px) {
  .hero-panel,
  .detail-layout,
  .insight-strip {
    grid-template-columns: 1fr;
  }

  .side-col {
    position: static;
  }
}

@media (max-width: 768px) {
  .commission-detail-page {
    padding: 18px 14px 32px;
  }

  .hero-panel,
  .section-card,
  .side-card,
  .modal-body {
    padding: 20px;
    border-radius: 22px;
  }

  .hero-title-row h1 {
    font-size: 28px;
  }

  .hero-title-row,
  .section-head,
  .msg-send,
  .identity-grid,
  .payment-top,
  .after-sale-top,
  .modal-actions {
    display: flex;
    flex-direction: column;
    align-items: stretch;
  }

  .msg-bubble {
    max-width: 100%;
  }

  .coupon-item {
    grid-template-columns: 1fr;
  }

  .coupon-left {
    min-height: 70px;
  }

  .btn-cancel,
  .btn-submit,
  .msg-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
