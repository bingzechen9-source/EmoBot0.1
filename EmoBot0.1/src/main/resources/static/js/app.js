(function () {
  const API_BASE = window.location.protocol === 'file:' ? 'http://localhost:8080' : '';
  const CONVERSATION_ID = 'default';

  const chatMessages = document.getElementById('chatMessages');
  const chatForm = document.getElementById('chatForm');
  const messageInput = document.getElementById('messageInput');
  const sendBtn = document.getElementById('sendBtn');
  const charCount = document.getElementById('charCount');

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  function removeWelcome() {
    const welcome = chatMessages.querySelector('.welcome-msg');
    if (welcome) welcome.remove();
  }

  function addMessage(role, content) {
    removeWelcome();
    const msg = document.createElement('div');
    msg.className = `msg ${role}`;
    const avatar = role === 'user' ? '👤' : '💭';
    const time = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    msg.innerHTML = `
      <div class="avatar">${avatar}</div>
      <div class="content-wrap">
        <div class="content">${escapeHtml(content)}</div>
        <div class="time">${time}</div>
      </div>
    `;
    chatMessages.appendChild(msg);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function addTypingMessage() {
    removeWelcome();
    const msg = document.createElement('div');
    msg.className = 'msg assistant';
    msg.id = 'loadingMsg';
    msg.innerHTML = `
      <div class="avatar">💭</div>
      <div class="content-wrap">
        <div class="content loading-dots"><span>.</span><span>.</span><span>.</span></div>
      </div>
    `;
    chatMessages.appendChild(msg);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function removeLoading() {
    const el = document.getElementById('loadingMsg');
    if (el) el.remove();
  }

  function updateCharCount() {
    if (charCount) charCount.textContent = messageInput.value.length;
  }

  async function sendMessage(text) {
    const t = (text || messageInput.value || '').trim();
    if (!t) return;
    messageInput.value = '';
    updateCharCount();
    sendBtn.disabled = true;
    addMessage('user', t);
    addTypingMessage();

    try {
      const res = await fetch(`${API_BASE}/api/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ conversationId: CONVERSATION_ID, message: t })
      });

      let data;
      try {
        data = await res.json();
      } catch (e) {
        throw new Error('响应不是有效的 JSON，状态: ' + res.status);
      }

      removeLoading();
      if (data.code === 200 && data.data) {
        addMessage('assistant', data.data);
      } else {
        const msg = data.message || ('HTTP ' + res.status) || '未知错误';
        addMessage('assistant', '抱歉，出现了一些问题：' + msg);
        console.error('API 错误:', res.status, data);
      }
    } catch (err) {
      removeLoading();
      const errMsg = err.message || '网络错误，请检查：1) 后端已启动 2) 访问地址为 http://localhost:8080/ 而非 file://';
      addMessage('assistant', '出错：' + errMsg);
      console.error('请求失败:', err);
    } finally {
      sendBtn.disabled = false;
      messageInput.focus();
    }
  }

  function clearChat() {
    const welcome = document.createElement('div');
    welcome.className = 'welcome-msg';
    welcome.innerHTML = `
      <div class="welcome-icon">✨</div>
      <h3>你好，我是 EmoBot</h3>
      <p>你的情感指导助手，支持多轮对话、记忆持久化，并能调用图片搜索、地图、呼吸练习等工具帮助你。</p>
      <p class="welcome-tip">试试左侧快捷工具，或直接告诉我你的想法～</p>
    `;
    chatMessages.innerHTML = '';
    chatMessages.appendChild(welcome);
    messageInput.focus();
  }

  chatForm.addEventListener('submit', (e) => {
    e.preventDefault();
    sendMessage();
  });

  messageInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  messageInput.addEventListener('input', updateCharCount);

  document.querySelectorAll('.mood-chip').forEach((btn) => {
    btn.addEventListener('click', () => {
      const text = btn.getAttribute('data-text');
      if (text) sendMessage(text);
    });
  });

  document.querySelectorAll('.nav-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      const prompt = btn.getAttribute('data-prompt');
      if (prompt) sendMessage(prompt);
    });
  });

  const clearBtn = document.getElementById('clearBtn');
  if (clearBtn) clearBtn.addEventListener('click', clearChat);

  updateCharCount();
})();
