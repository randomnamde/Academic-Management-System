import { describe, it, expect } from 'vitest'
import {
  phoneRule,
  idCardRule,
  emailRule,
  passwordRule,
  studentIdRule
} from '@/utils/validators'

describe('validators', () => {
  describe('phoneRule', () => {
    it('should validate Chinese mobile numbers', () => {
      const rule = phoneRule()
      const validator = rule.validator

      // Valid phone numbers
      expect(validator({}, '13812345678')).toBe(true)
      expect(validator({}, '15912345678')).toBe(true)
      expect(validator({}, '18812345678')).toBe(true)

      // Invalid phone numbers
      expect(validator({}, '12812345678')).toBe(false)
      expect(validator({}, '1381234567')).toBe(false)
      expect(validator({}, '138123456789')).toBe(false)
      expect(validator({}, 'abc12345678')).toBe(false)
    })
  })

  describe('idCardRule', () => {
    it('should validate Chinese ID card numbers', () => {
      const rule = idCardRule()
      const validator = rule.validator

      // Valid ID cards (18 digits)
      expect(validator({}, '110101199001011234')).toBe(true)

      // Invalid ID cards
      expect(validator({}, '123456789012345678')).toBe(false)
      expect(validator({}, '11010119900101123')).toBe(false)
      expect(validator({}, 'abc123456789012345')).toBe(false)
    })
  })

  describe('emailRule', () => {
    it('should validate email addresses', () => {
      const rule = emailRule()
      const validator = rule.validator

      // Valid emails
      expect(validator({}, 'test@example.com')).toBe(true)
      expect(validator({}, 'user.name@domain.co.uk')).toBe(true)
      expect(validator({}, 'user+tag@example.com')).toBe(true)

      // Invalid emails
      expect(validator({}, 'invalid')).toBe(false)
      expect(validator({}, '@example.com')).toBe(false)
      expect(validator({}, 'test@')).toBe(false)
    })
  })

  describe('passwordRule', () => {
    it('should validate password strength', () => {
      const rule = passwordRule()
      const validator = rule.validator

      // Valid passwords
      expect(validator({}, 'Abc123!')).toBe(true)
      expect(validator({}, 'Password1@')).toBe(true)

      // Invalid passwords (too short)
      expect(validator({}, 'Abc12!')).toBe(false)

      // Invalid passwords (no number)
      expect(validator({}, 'Abcdefg!')).toBe(false)

      // Invalid passwords (no uppercase)
      expect(validator({}, 'abcdefg1!')).toBe(false)

      // Invalid passwords (no lowercase)
      expect(validator({}, 'ABCDEFG1!')).toBe(false)

      // Invalid passwords (no special char)
      expect(validator({}, 'Abcdefg12')).toBe(false)
    })
  })

  describe('studentIdRule', () => {
    it('should validate student IDs', () => {
      const rule = studentIdRule()
      const validator = rule.validator

      // Valid student IDs
      expect(validator({}, '2021001001')).toBe(true)
      expect(validator({}, 'S2021001')).toBe(true)

      // Invalid student IDs
      expect(validator({}, '123')).toBe(false)
      expect(validator({}, 'abc')).toBe(false)
    })
  })
})
