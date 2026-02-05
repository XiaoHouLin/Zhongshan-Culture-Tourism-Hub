const buildUidKey = uid => (uid ? `zs_messages_${uid}` : '');
const buildUserKey = username => (username ? `zs_messages_user_${username}` : '');

const parseMessages = raw => {
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch (error) {
    return [];
  }
};

const saveMessages = (key, messages) => {
  if (!key) return;
  localStorage.setItem(key, JSON.stringify(messages));
};

export const formatMessageTime = (date = new Date()) => {
  const pad = value => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
    `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

export const loadMessages = ({ uid, username } = {}) => {
  const uidKey = buildUidKey(uid);
  const userKey = buildUserKey(username);
  let messages = uidKey ? parseMessages(localStorage.getItem(uidKey)) : [];
  if (userKey) {
    const userMessages = parseMessages(localStorage.getItem(userKey));
    if (userMessages.length) {
      const merged = [...messages];
      userMessages.forEach(message => {
        if (!merged.some(item => item.id === message.id)) {
          merged.push(message);
        }
      });
      messages = merged;
      if (uidKey) {
        saveMessages(uidKey, messages);
        localStorage.removeItem(userKey);
      } else {
        saveMessages(userKey, messages);
      }
    }
  }
  return messages;
};

export const addMessage = ({ uid, username } = {}, message) => {
  const key = uid ? buildUidKey(uid) : buildUserKey(username);
  if (!key || !message || !message.id) return;
  const messages = parseMessages(localStorage.getItem(key));
  if (!messages.some(item => item.id === message.id)) {
    messages.unshift(message);
    saveMessages(key, messages);
  }
};

export const updateMessage = ({ uid, username } = {}, id, updater) => {
  const key = uid ? buildUidKey(uid) : buildUserKey(username);
  if (!key || !id || typeof updater !== 'function') return [];
  const messages = parseMessages(localStorage.getItem(key));
  const next = messages.map(item => (item.id === id ? updater(item) : item));
  saveMessages(key, next);
  return next;
};

export const removeMessage = ({ uid, username } = {}, id) => {
  const key = uid ? buildUidKey(uid) : buildUserKey(username);
  if (!key || !id) return [];
  const messages = parseMessages(localStorage.getItem(key));
  const next = messages.filter(item => item.id !== id);
  saveMessages(key, next);
  return next;
};
