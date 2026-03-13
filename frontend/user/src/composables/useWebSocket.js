import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { Client } from '@stomp/stompjs'
import { useUserStore } from '@/stores/user'

let stompClient = null
let reconnectTimer = null
let chatCallback = null

/**
 * WebSocket 通知与私信服务
 * 使用 STOMP over SockJS 连接后端实时通知推送和私信推送
 */
export function useWebSocketNotification() {
  const userStore = useUserStore()

  /**
   * 建立 WebSocket 连接
   * @param {Function} onNotification - 收到通知的回调函数
   */
  function connect(onNotification) {
    if (!userStore.isAuthenticated || !userStore.userInfo?.id) {
      return
    }

    disconnect() // 先断开旧连接

    const userId = userStore.userInfo.id
    // 通过网关连接 WebSocket；开发环境直连 notification-service:8084
    const wsUrl = `${window.location.protocol}//${window.location.hostname}:8080/ws/notifications`

    stompClient = new Client({
      // 使用 SockJS 工厂
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,       // 自动重连间隔 5 秒
      heartbeatIncoming: 10000,   // 心跳：10 秒
      heartbeatOutgoing: 10000,
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('[WS]', str)
        }
      },
      onConnect: () => {
        console.log('[WS] 已连接')
        // 订阅当前用户的通知频道
        stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
          try {
            const notification = JSON.parse(message.body)
            console.log('[WS] 收到通知:', notification)
            if (typeof onNotification === 'function') {
              onNotification(notification)
            }
          } catch (e) {
            console.error('[WS] 解析通知失败:', e)
          }
        })

        // 订阅当前用户的私信频道
        stompClient.subscribe(`/topic/chat/${userId}`, (message) => {
          try {
            const chatMsg = JSON.parse(message.body)
            console.log('[WS] 收到私信:', chatMsg)
            if (typeof chatCallback === 'function') {
              chatCallback(chatMsg)
            }
          } catch (e) {
            console.error('[WS] 解析私信失败:', e)
          }
        })
      },
      onDisconnect: () => {
        console.log('[WS] 已断开')
      },
      onStompError: (frame) => {
        console.error('[WS] STOMP 错误:', frame.headers?.message)
      }
    })

    stompClient.activate()
  }

  /**
   * 注册私信消息回调
   * @param {Function} callback - 收到私信时的回调函数
   */
  function onChatMessage(callback) {
    chatCallback = callback
  }

  /**
   * 取消私信消息回调
   */
  function offChatMessage() {
    chatCallback = null
  }

  /**
   * 断开 WebSocket 连接
   */
  function disconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (stompClient) {
      try {
        stompClient.deactivate()
      } catch (e) {
        // ignore
      }
      stompClient = null
    }
  }

  /**
   * 检查是否已连接
   */
  function isConnected() {
    return stompClient?.connected ?? false
  }

  return { connect, disconnect, isConnected, onChatMessage, offChatMessage }
}
