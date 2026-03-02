const fs = require('fs')
const path = require('path')

const root = path.resolve(__dirname, '../src')
const exts = new Set(['.vue', '.js', '.scss', '.css', '.json'])
const suspicious = ['鍒', '瀛', '绯', '璇', '鏃', '鎿', '锛', '锟']

const violations = []

function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  for (const entry of entries) {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      walk(full)
      continue
    }

    if (!exts.has(path.extname(entry.name))) continue

    const content = fs.readFileSync(full, 'utf8')
    if (content.includes('\uFFFD')) {
      violations.push(`${full} 含有 replacement 字符`)
      continue
    }

    for (const marker of suspicious) {
      if (content.includes(marker)) {
        violations.push(`${full} 含可疑乱码片段: ${marker}`)
        break
      }
    }
  }
}

walk(root)

if (violations.length) {
  console.error('发现编码问题:')
  violations.forEach((item) => console.error(`- ${item}`))
  process.exit(1)
}

console.log('UTF-8 检查通过')
