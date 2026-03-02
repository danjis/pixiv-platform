import request from '@/utils/request'

/**
 * 获取图形验证码
 */
export function getImageCaptcha() {
  return request({
    url: '/api/captcha/image',
    method: 'get'
  })
}

/**
 * 发送邮箱验证码
 * @param {string} email - 邮箱地址
 * @param {string} captchaId - 图形验证码ID
 * @param {string} captchaCode - 图形验证码
 */
export function sendEmailCode(email, captchaId, captchaCode) {
  return request({
    url: '/api/captcha/email',
    method: 'post',
    params: {
      email,
      captchaId,
      captchaCode
    }
  })
}
