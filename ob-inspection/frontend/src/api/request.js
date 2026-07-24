import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一 axios 实例：解包 Result{code,msg,data}，非 0 统一报错提示
const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) {
        return body.data
      }
      ElMessage.error(body.msg || '请求失败')
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return body
  },
  (error) => {
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
