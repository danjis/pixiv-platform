import request from '@/utils/request'

/**
 * 上传图片文件（作品图片，存储到 artworks/ 目录）
 * @param {File} file - 图片文件
 * @returns {Promise} 返回图片 URL
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000 // 上传文件超时时间设置为 60 秒
  })
}

/**
 * 上传头像文件（存储到 avatars/ 目录，不生成缩略图）
 * @param {File} file - 头像图片文件
 * @returns {Promise} 返回头像 URL
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/api/files/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000
  })
}

/**
 * 上传文件（uploadImage 的别名）
 * @param {FormData} formData - 包含文件的 FormData 对象
 * @returns {Promise} 返回文件 URL
 */
export function uploadFile(formData) {
  return request({
    url: '/api/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 60000 // 上传文件超时时间设置为 60 秒
  })
}

/**
 * 批量上传图片
 * @param {File[]} files - 图片文件数组
 * @returns {Promise} 返回图片 URL 数组
 */
export function uploadImages(files) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: '/api/files/upload-batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 120000 // 批量上传超时时间设置为 120 秒
  })
}
