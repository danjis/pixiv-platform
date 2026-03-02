/**
 * 验证邮箱格式
 * @param {string} email - 邮箱地址
 * @returns {boolean}
 */
export function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 验证密码强度
 * @param {string} password - 密码
 * @returns {boolean}
 */
export function isValidPassword(password) {
  // 至少 8 个字符，包含字母和数字
  return password.length >= 8 && /[a-zA-Z]/.test(password) && /[0-9]/.test(password)
}

/**
 * 验证用户名格式
 * @param {string} username - 用户名
 * @returns {boolean}
 */
export function isValidUsername(username) {
  // 3-20 个字符，只能包含字母、数字、下划线
  const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/
  return usernameRegex.test(username)
}

/**
 * 验证 URL 格式
 * @param {string} url - URL 地址
 * @returns {boolean}
 */
export function isValidUrl(url) {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}
