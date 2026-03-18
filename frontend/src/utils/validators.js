/**
 * 表单验证工具类
 * 提供常用的表单验证规则
 */

/**
 * 手机号验证规则（中国大陆）
 */
export const phoneRule = (message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // 中国大陆手机号正则
      const phoneRegex = /^1[3-9]\d{9}$/

      if (!phoneRegex.test(value)) {
        callback(new Error(message || '请输入正确的手机号码'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 身份证号验证规则（中国大陆）
 */
export const idCardRule = (message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // 身份证号正则（15位或18位）
      const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/

      if (!idCardRegex.test(value)) {
        callback(new Error(message || '请输入正确的身份证号码'))
      } else {
        // 验证18位身份证的校验码
        if (value.length === 18) {
          if (!validateIdCardCheckCode(value)) {
            callback(new Error(message || '请输入正确的身份证号码'))
            return
          }
        }
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 验证18位身份证校验码
 */
function validateIdCardCheckCode(idCard) {
  // 加权因子
  const weight = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  // 校验码对应值
  const checkCodeMap = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']

  let sum = 0
  for (let i = 0; i < 17; i++) {
    sum += parseInt(idCard.charAt(i)) * weight[i]
  }

  const checkCode = checkCodeMap[sum % 11]
  return checkCode === idCard.charAt(17).toUpperCase()
}

/**
 * 邮箱验证规则
 */
export const emailRule = (message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // 邮箱正则
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

      if (!emailRegex.test(value)) {
        callback(new Error(message || '请输入正确的邮箱地址'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 密码强度验证
 */
export const passwordRule = (options = {}) => {
  const {
    minLength = 6,
    maxLength = 20,
    requireNumber = true,
    requireLetter = true,
    requireSpecial = false,
    message = '密码强度不符合要求'
  } = options

  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // 长度验证
      if (value.length < minLength || value.length > maxLength) {
        callback(new Error(message || `密码长度应在 ${minLength}-${maxLength} 位之间`))
        return
      }

      // 数字验证
      if (requireNumber && !/\d/.test(value)) {
        callback(new Error(message || '密码必须包含数字'))
        return
      }

      // 字母验证
      if (requireLetter && !/[a-zA-Z]/.test(value)) {
        callback(new Error(message || '密码必须包含字母'))
        return
      }

      // 特殊字符验证
      if (requireSpecial && !/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value)) {
        callback(new Error(message || '密码必须包含特殊字符'))
        return
      }

      callback()
    },
    trigger: 'blur'
  }
}

/**
 * 确认密码验证
 */
export const confirmPasswordRule = (passwordField, message) => {
  return {
    validator: (rule, value, callback, source) => {
      if (!value) {
        callback()
        return
      }

      const password = source[passwordField]
      if (value !== password) {
        callback(new Error(message || '两次输入的密码不一致'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 用户名验证规则
 */
export const usernameRule = (message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // 用户名：4-20位，只能包含字母、数字、下划线
      const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/

      if (!usernameRegex.test(value)) {
        callback(new Error(message || '用户名应为4-20位字母、数字或下划线'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 学号验证规则
 */
export const studentIdRule = (message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // 学号：8-12位数字
      const studentIdRegex = /^\d{8,12}$/

      if (!studentIdRegex.test(value)) {
        callback(new Error(message || '请输入正确的学号'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 年龄验证规则
 */
export const ageRule = (min = 10, max = 100, message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value && value !== 0) {
        callback()
        return
      }

      const age = parseInt(value)
      if (isNaN(age) || age < min || age > max) {
        callback(new Error(message || `年龄应在 ${min}-${max} 岁之间`))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * QQ号验证规则
 */
export const qqRule = (message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // QQ号：5-12位数字
      const qqRegex = /^[1-9]\d{4,11}$/

      if (!qqRegex.test(value)) {
        callback(new Error(message || '请输入正确的QQ号'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 微信号验证规则
 */
export const wechatRule = (message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // 微信号：6-20位，以字母开头，可包含字母、数字、下划线、减号
      const wechatRegex = /^[a-zA-Z][-_a-zA-Z0-9]{5,19}$/

      if (!wechatRegex.test(value)) {
        callback(new Error(message || '请输入正确的微信号'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * URL验证规则
 */
export const urlRule = (message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      // URL正则
      const urlRegex = /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/

      if (!urlRegex.test(value)) {
        callback(new Error(message || '请输入正确的URL地址'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 日期范围验证
 */
export const dateRangeRule = (startDateField, endDateField, message) => {
  return {
    validator: (rule, value, callback, source) => {
      if (!source[startDateField] || !source[endDateField]) {
        callback()
        return
      }

      const startDate = new Date(source[startDateField])
      const endDate = new Date(source[endDateField])

      if (startDate > endDate) {
        callback(new Error(message || '开始日期不能晚于结束日期'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 数字范围验证
 */
export const numberRangeRule = (min, max, message) => {
  return {
    validator: (rule, value, callback) => {
      if (!value && value !== 0) {
        callback()
        return
      }

      const num = parseFloat(value)
      if (isNaN(num) || num < min || num > max) {
        callback(new Error(message || `数值应在 ${min}-${max} 之间`))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }
}

/**
 * 异步验证（检查账号是否已存在）
 */
export const asyncValidator = (checkFn, message) => {
  return {
    validator: async (rule, value, callback) => {
      if (!value) {
        callback()
        return
      }

      try {
        const exists = await checkFn(value)
        if (exists) {
          callback(new Error(message || '该账号已存在'))
        } else {
          callback()
        }
      } catch (error) {
        callback(new Error('验证失败，请重试'))
      }
    },
    trigger: 'blur'
  }
}

/**
 * 导出所有验证规则
 */
export const validators = {
  phone: phoneRule,
  idCard: idCardRule,
  email: emailRule,
  password: passwordRule,
  confirmPassword: confirmPasswordRule,
  username: usernameRule,
  studentId: studentIdRule,
  age: ageRule,
  qq: qqRule,
  wechat: wechatRule,
  url: urlRule,
  dateRange: dateRangeRule,
  numberRange: numberRangeRule,
  async: asyncValidator
}

export default validators
