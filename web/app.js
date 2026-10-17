let authMode = 'login';
let currentUser = null;
let activeChatUser = null;
let allUsers = [];
let messagePollingTimer = null;
let soundEnabled = true;

// =========================================
// PASSWORD VISIBILITY TOGGLE
// =========================================
function togglePasswordVisibility() {
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eye-icon');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        // Eye-slash icon
        eyeIcon.innerHTML = `
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
            <line x1="1" y1="1" x2="23" y2="23"></line>
        `;
    } else {
        passwordInput.type = 'password';
        // Standard eye icon
        eyeIcon.innerHTML = `
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
        `;
    }
}

// =========================================
// WEB AUDIO API SYNTHESIZER
// =========================================
let audioCtx = null;
function playChime(isSend = true) {
    if (!soundEnabled) return;
    try {
        if (!audioCtx) {
            audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        }
        if (audioCtx.state === 'suspended') {
            audioCtx.resume();
        }

        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();

        osc.connect(gain);
        gain.connect(audioCtx.destination);

        const now = audioCtx.currentTime;
        if (isSend) {
            osc.frequency.setValueAtTime(620, now);
            osc.frequency.exponentialRampToValueAtTime(920, now + 0.11);
        } else {
            osc.frequency.setValueAtTime(780, now);
            osc.frequency.exponentialRampToValueAtTime(560, now + 0.13);
        }

        gain.gain.setValueAtTime(0.08, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.13);

        osc.start(now);
        osc.stop(now + 0.14);
    } catch (e) {
        // audio context blocked or unsupported
    }
}

function toggleSound() {
    soundEnabled = !soundEnabled;
    const btn = document.getElementById('sound-toggle-btn');
    btn.innerHTML = soundEnabled ? '🔔 <span>Audio On</span>' : '🔕 <span>Muted</span>';
}

function insertEmoji(emoji) {
    const input = document.getElementById('message-input');
    input.value += emoji;
    input.focus();
}

// Avatar Gradient Generator
const GRADIENTS = [
    'linear-gradient(135deg, #6366f1, #a855f7)',
    'linear-gradient(135deg, #3b82f6, #06b6d4)',
    'linear-gradient(135deg, #10b981, #059669)',
    'linear-gradient(135deg, #f59e0b, #d97706)',
    'linear-gradient(135deg, #ec4899, #8b5cf6)',
    'linear-gradient(135deg, #14b8a6, #3b82f6)'
];

function getAvatarGradient(name) {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % GRADIENTS.length;
    return GRADIENTS[index];
}

// =========================================
// AUTHENTICATION LOGIC
// =========================================
function switchAuthTab(mode) {
    authMode = mode;
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const submitBtn = document.getElementById('auth-submit-btn');
    const alertBox = document.getElementById('auth-alert');

    alertBox.className = 'alert-box hidden';
    alertBox.textContent = '';

    if (mode === 'login') {
        tabLogin.classList.add('active');
        tabRegister.classList.remove('active');
        submitBtn.querySelector('span').textContent = 'Sign In to Account';
    } else {
        tabRegister.classList.add('active');
        tabLogin.classList.remove('active');
        submitBtn.querySelector('span').textContent = 'Create New Account';
    }
}

async function handleAuthSubmit(event) {
    event.preventDefault();
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    const username = usernameInput.value.trim().toLowerCase();
    const password = passwordInput.value;

    if (!username || !password) {
        showAlert('Please enter both username and password.', 'error');
        return;
    }

    const endpoint = authMode === 'login' ? '/api/login' : '/api/register';

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            currentUser = username;
            showAlert(data.message, 'success');
            setTimeout(() => {
                initChatView();
            }, 350);
        } else {
            showAlert(data.error || 'Authentication failed.', 'error');
        }
    } catch (err) {
        console.error(err);
        showAlert('Could not connect to the server.', 'error');
    }
}

function showAlert(message, type) {
    const alertBox = document.getElementById('auth-alert');
    alertBox.textContent = message;
    alertBox.className = `alert-box ${type}`;
}

// =========================================
// CHAT VIEW INITIALIZATION
// =========================================
function initChatView() {
    document.getElementById('auth-view').classList.add('hidden');
    document.getElementById('chat-view').classList.remove('hidden');

    document.getElementById('current-user-name').textContent = '@' + currentUser;
    const avatarEl = document.getElementById('current-user-avatar');
    avatarEl.textContent = currentUser.charAt(0).toUpperCase();
    avatarEl.style.background = getAvatarGradient(currentUser);

    loadUsers();
    checkAdminStatus();
}

function logout() {
    if (messagePollingTimer) {
        clearInterval(messagePollingTimer);
        messagePollingTimer = null;
    }
    currentUser = null;
    activeChatUser = null;
    allUsers = [];

    const frame = document.querySelector('.app-frame');
    if (frame) frame.classList.remove('in-chat');

    document.getElementById('admin-panel-btn').classList.add('hidden');
    document.getElementById('admin-modal').classList.add('hidden');

    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
    document.getElementById('auth-alert').className = 'alert-box hidden';

    document.getElementById('chat-view').classList.add('hidden');
    document.getElementById('auth-view').classList.remove('hidden');
}

// =========================================
// USERS MANAGEMENT
// =========================================
async function loadUsers() {
    try {
        const res = await fetch(`/api/users?current=${encodeURIComponent(currentUser)}`);
        const users = await res.json();
        allUsers = users;
        renderUsersList(allUsers);
    } catch (err) {
        console.error('Failed to load users:', err);
    }
}

function renderUsersList(users) {
    const listContainer = document.getElementById('users-list');
    listContainer.innerHTML = '';

    // Pinned @SphereAI Assistant
    const botItem = document.createElement('div');
    botItem.className = `user-card-item ai-bot-card ${activeChatUser === 'SphereAI' ? 'active' : ''}`;
    botItem.onclick = () => selectUser('SphereAI');
    botItem.innerHTML = `
        <div class="avatar ai-avatar" style="width: 40px; height: 40px; font-size: 18px; flex-shrink: 0; background: linear-gradient(135deg, #8b5cf6, #3b82f6); box-shadow: 0 0 15px rgba(139, 92, 246, 0.4);">🤖</div>
        <div class="user-card-meta">
            <div class="user-row-top">
                <span class="user-item-username" style="color: #c4b5fd; font-weight: 800;">@SphereAI <span class="ai-badge">AI BOT</span></span>
                <span class="user-item-timestamp" style="color: #a78bfa;">24/7</span>
            </div>
            <div class="user-row-bottom">
                <span class="user-item-snippet" style="color: #a78bfa;">⚡ Instant answers & Java help</span>
            </div>
        </div>
    `;
    listContainer.appendChild(botItem);

    // Filter out duplicate bot from regular users list
    const realUsers = users.filter(u => u.username.toLowerCase() !== 'sphereai');

    if (realUsers.length === 0) {
        const emptyNotice = document.createElement('div');
        emptyNotice.style.cssText = 'padding: 22px 16px; text-align: center; color: var(--text-dim); font-size: 12.5px; line-height: 1.5;';
        emptyNotice.innerHTML = 'No other friends online.<br>Chat with <b>@SphereAI</b> above or invite friends!';
        listContainer.appendChild(emptyNotice);
        return;
    }

    realUsers.forEach(user => {
        const item = document.createElement('div');
        item.className = `user-card-item ${activeChatUser === user.username ? 'active' : ''}`;
        item.onclick = () => selectUser(user.username);

        const initial = user.username.charAt(0).toUpperCase();
        const gradient = getAvatarGradient(user.username);

        item.innerHTML = `
            <div class="avatar" style="width: 40px; height: 40px; font-size: 14px; background: ${gradient}; flex-shrink: 0;">${initial}</div>
            <div class="user-card-meta">
                <div class="user-row-top">
                    <span class="user-item-username">@${user.username}</span>
                    <span class="user-item-timestamp">Active</span>
                </div>
                <div class="user-row-bottom">
                    <span class="user-item-snippet">Tap to open encrypted chat</span>
                </div>
            </div>
        `;
        listContainer.appendChild(item);
    });
}

function filterUsers() {
    const query = document.getElementById('search-input').value.toLowerCase();
    const filtered = allUsers.filter(u => u.username.toLowerCase().includes(query));
    renderUsersList(filtered);
}

// =========================================
// ACTIVE CHAT & MESSAGING
// =========================================
function selectUser(username) {
    activeChatUser = username;

    const frame = document.querySelector('.app-frame');
    if (frame) frame.classList.add('in-chat');

    document.getElementById('no-chat-selected').classList.add('hidden');
    document.getElementById('active-chat-wrapper').classList.remove('hidden');

    const chatAvatar = document.getElementById('active-chat-avatar');
    if (username === 'SphereAI') {
        document.getElementById('active-chat-username').innerHTML = '@SphereAI <span class="ai-badge">AI BOT</span>';
        chatAvatar.textContent = '🤖';
        chatAvatar.style.background = 'linear-gradient(135deg, #8b5cf6, #3b82f6)';
        chatAvatar.style.boxShadow = '0 0 15px rgba(139, 92, 246, 0.4)';
    } else {
        document.getElementById('active-chat-username').textContent = '@' + username;
        chatAvatar.textContent = username.charAt(0).toUpperCase();
        chatAvatar.style.background = getAvatarGradient(username);
        chatAvatar.style.boxShadow = 'none';
    }

    renderUsersList(allUsers);

    lastRenderedMessagesCount = -1;
    loadMessages();

    if (messagePollingTimer) {
        clearInterval(messagePollingTimer);
    }
    // Poll for new incoming messages every 1.5 seconds
    messagePollingTimer = setInterval(loadMessages, 1500);

    document.getElementById('message-input').focus();
}

function backToSidebar() {
    const frame = document.querySelector('.app-frame');
    if (frame) frame.classList.remove('in-chat');

    activeChatUser = null;
    const noChat = document.getElementById('no-chat-selected');
    const activeChat = document.getElementById('active-chat-wrapper');
    if (noChat) noChat.classList.remove('hidden');
    if (activeChat) activeChat.classList.add('hidden');

    if (messagePollingTimer) {
        clearInterval(messagePollingTimer);
        messagePollingTimer = null;
    }
    renderUsersList(allUsers);
}

let lastRenderedMessagesCount = -1;

async function loadMessages() {
    if (!currentUser || !activeChatUser) return;

    try {
        const res = await fetch(`/api/messages?user1=${encodeURIComponent(currentUser)}&user2=${encodeURIComponent(activeChatUser)}`);
        const messages = await res.json();

        if (messages.length !== lastRenderedMessagesCount) {
            const hadMessages = lastRenderedMessagesCount > 0;
            const isNewIncoming = messages.length > lastRenderedMessagesCount && hadMessages;

            lastRenderedMessagesCount = messages.length;
            renderMessages(messages);

            // Play incoming sound if message arrived from friend
            if (isNewIncoming) {
                const latest = messages[messages.length - 1];
                if (latest.sender.toLowerCase() !== currentUser.toLowerCase()) {
                    playChime(false);
                }
            }
        }
    } catch (err) {
        console.error('Failed to load messages:', err);
    }
}

function renderMessages(messages) {
    const container = document.getElementById('messages-container');
    container.innerHTML = '';

    if (messages.length === 0) {
        container.innerHTML = `
            <div style="margin: auto; text-align: center; color: var(--text-dim); font-size: 13px;">
                No messages yet. Say hello to @${activeChatUser}! 👋
            </div>
        `;
        return;
    }

    // Date Divider Pill
    const dateDivider = document.createElement('div');
    dateDivider.className = 'date-divider';
    dateDivider.innerHTML = '<span class="date-badge">TODAY</span>';
    container.appendChild(dateDivider);

    messages.forEach(msg => {
        const isSent = msg.sender.toLowerCase() === currentUser.toLowerCase();
        const bubble = document.createElement('div');
        bubble.className = `message-bubble ${isSent ? 'sent' : 'received'}`;

        if (!isSent) {
            const senderTag = document.createElement('div');
            senderTag.className = 'bubble-sender';
            senderTag.textContent = '@' + msg.sender;
            bubble.appendChild(senderTag);
        }

        const textContent = document.createElement('div');
        textContent.className = 'bubble-text';
        textContent.textContent = msg.content;
        bubble.appendChild(textContent);

        const footer = document.createElement('div');
        footer.className = 'bubble-footer';

        const timeSpan = document.createElement('span');
        timeSpan.textContent = msg.timestamp || 'Now';
        footer.appendChild(timeSpan);

        if (isSent) {
            const ticks = document.createElement('span');
            ticks.className = 'double-ticks';
            ticks.textContent = ' ✓✓';
            footer.appendChild(ticks);
        }

        bubble.appendChild(footer);
        container.appendChild(bubble);
    });

    container.scrollTop = container.scrollHeight;
}

async function handleSendMessage(event) {
    event.preventDefault();
    const input = document.getElementById('message-input');
    const content = input.value.trim();

    if (!content || !activeChatUser) return;

    input.value = '';

    // Play instant sound
    playChime(true);

    try {
        const res = await fetch('/api/send', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sender: currentUser,
                receiver: activeChatUser,
                content: content
            })
        });

        if (res.ok) {
            loadMessages();
        }
    } catch (err) {
        console.error('Error sending message:', err);
    }
}

// =========================================
// SUPER ADMIN CONSOLE CONTROLLER
// =========================================
async function checkAdminStatus() {
    if (!currentUser) return;
    try {
        const res = await fetch(`/api/admin/check?user=${encodeURIComponent(currentUser)}`);
        const data = await res.json();
        if (data.isAdmin) {
            document.getElementById('admin-panel-btn').classList.remove('hidden');
            document.getElementById('current-user-name').textContent = '@' + currentUser + ' 👑';
        } else {
            document.getElementById('admin-panel-btn').classList.add('hidden');
        }
    } catch (err) {
        console.error('Failed to verify admin status:', err);
    }
}

function openAdminModal() {
    document.getElementById('admin-modal').classList.remove('hidden');
    loadAdminDashboard();
}

function closeAdminModal() {
    document.getElementById('admin-modal').classList.add('hidden');
    document.getElementById('admin-feedback').classList.add('hidden');
}

function handleModalBackdropClick(event) {
    if (event.target.id === 'admin-modal') {
        closeAdminModal();
    }
}

async function loadAdminDashboard() {
    if (!currentUser) return;
    try {
        const res = await fetch(`/api/admin/stats?admin=${encodeURIComponent(currentUser)}`);
        const data = await res.json();

        if (data.success) {
            document.getElementById('stat-total-users').textContent = data.totalUsers;
            document.getElementById('stat-total-messages').textContent = data.totalMessages;

            renderAdminUserRows(data.users || []);
        }
    } catch (err) {
        console.error('Failed to load admin dashboard:', err);
    }
}

function renderAdminUserRows(users) {
    const container = document.getElementById('admin-users-table');
    container.innerHTML = '';

    if (users.length === 0) {
        container.innerHTML = '<div style="padding: 16px; text-align: center; color: var(--text-dim); font-size: 13px;">No users found</div>';
        return;
    }

    users.forEach(user => {
        const row = document.createElement('div');
        row.className = 'admin-user-row';

        const isSuperAdmin = user.username.toLowerCase() === 'alokrana' || user.username.toLowerCase() === 'admin';
        const initial = user.username.charAt(0).toUpperCase();
        const gradient = getAvatarGradient(user.username);

        row.innerHTML = `
            <div class="admin-user-info">
                <div class="avatar" style="width: 32px; height: 32px; font-size: 12px; background: ${gradient};">${initial}</div>
                <div>
                    <span class="admin-user-name">@${user.username} ${isSuperAdmin ? '👑 (Admin)' : ''}</span>
                </div>
            </div>
            <div class="admin-actions-cell">
                ${!isSuperAdmin ? `
                    <button class="btn-admin-act btn-warn" onclick="sendWarningByAdmin('${user.username}')" title="Send Official Notice">
                        ⚠️ Warn
                    </button>
                    <button class="btn-admin-act btn-delete" onclick="deleteUserByAdmin('${user.username}')" title="Delete User">
                        🗑️ Delete
                    </button>
                ` : '<span style="font-size: 11px; color: var(--accent-emerald); font-weight: 700;">Protected</span>'}
            </div>
        `;
        container.appendChild(row);
    });
}

async function deleteUserByAdmin(targetUsername) {
    if (!confirm(`Are you sure you want to permanently delete user @${targetUsername} and their chat history?`)) {
        return;
    }

    try {
        const res = await fetch('/api/admin/delete-user', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ admin: currentUser, target: targetUsername })
        });
        const data = await res.json();

        showAdminFeedback(data.message || data.error);
        if (data.success) {
            loadAdminDashboard();
            loadUsers();
            if (activeChatUser === targetUsername) {
                document.getElementById('no-chat-selected').classList.remove('hidden');
                document.getElementById('active-chat-wrapper').classList.add('hidden');
                activeChatUser = null;
            }
        }
    } catch (err) {
        showAdminFeedback('Failed to delete user.');
    }
}

async function sendWarningByAdmin(targetUsername) {
    const warningText = prompt(`Enter official admin notice for @${targetUsername}:`, 'Please follow community rules and maintain respect.');
    if (!warningText || !warningText.trim()) return;

    try {
        const res = await fetch('/api/admin/warning', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ admin: currentUser, target: targetUsername, warning: warningText.trim() })
        });
        const data = await res.json();
        showAdminFeedback(data.message || data.error);
    } catch (err) {
        showAdminFeedback('Failed to send warning.');
    }
}

function showAdminFeedback(msg) {
    const el = document.getElementById('admin-feedback');
    el.textContent = msg;
    el.classList.remove('hidden');
    setTimeout(() => {
        el.classList.add('hidden');
    }, 4000);
}
