$ErrorActionPreference = "Stop"

if (-not $env:AI_DASHSCOPE_API_KEY) {
    $secureKey = Read-Host "请输入 DashScope API Key" -AsSecureString
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureKey)
    try {
        $env:AI_DASHSCOPE_API_KEY = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}

$env:AI_DASHSCOPE_ENABLED = "true"
$env:AI_DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"
$env:AI_DASHSCOPE_MODEL = "qwen-plus"

Write-Host "千问 AI 配置已加载，正在启动后端..." -ForegroundColor Green
mvn spring-boot:run
