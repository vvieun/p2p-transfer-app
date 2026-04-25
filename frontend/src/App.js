import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

const apiClient = axios.create({
  baseURL: '/api',
});

function App() {
  const [user, setUser] = useState(null);
  const [mode, setMode] = useState('login');
  const [auth, setAuth] = useState({ username: '', password: '' });
  const [newAccountBalance, setNewAccountBalance] = useState('');
  const [transfer, setTransfer] = useState({ fromAccountNumber: '', toAccountNumber: '', amount: '' });
  const [accounts, setAccounts] = useState([]);
  const [logs, setLogs] = useState([]);

  const writeLog = (message, type = 'success', executionTimeMs = null) => {
    const timestamp = new Date().toLocaleTimeString();
    const duration = executionTimeMs ? ` (${executionTimeMs} мс)` : '';
    setLogs((prev) => [{ message: `[${timestamp}] ${message}${duration}`, type }, ...prev]);
  };

  const getErrorMessage = (error) => {
    const payload = error?.response?.data;
    if (!payload) return error?.message || 'произошла неизвестная ошибка';
    if (payload?.message) return payload.message;
    return typeof payload === 'string' ? payload : JSON.stringify(payload);
  };

  const callApi = async (config, successMessage) => {
    try {
      const { data } = await apiClient(config);
      if (successMessage) {
        const text = typeof successMessage === 'function' ? successMessage(data.data) : successMessage;
        writeLog(text, 'success', data.executionTimeMs);
      }
      return data;
    } catch (error) {
      writeLog(getErrorMessage(error), 'error', error?.response?.data?.executionTimeMs);
      return null;
    }
  };

  const updateAuth = (e) => setAuth((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  const updateTransfer = (e) => setTransfer((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  const toKopecks = (value) => Math.round(Number(value || 0) * 100);

  const loadAccounts = async (userId) => {
    const response = await callApi({ method: 'get', url: `/accounts/user/${userId}` }, 'счета обновлены');
    if (response) setAccounts(response.data || []);
  };

  const handleAuth = async (e) => {
    e.preventDefault();
    const isLogin = mode === 'login';
    const response = await callApi(
      { method: 'post', url: isLogin ? '/users/login' : '/users/register', data: auth },
      (data) => `${isLogin ? 'с возвращением' : 'регистрация прошла успешно'}, ${data.username}`
    );
    if (!response) return;
    const nextUser = isLogin ? response.data : { userId: response.data.id, username: response.data.username };
    setUser(nextUser);
    if (isLogin) await loadAccounts(nextUser.userId);
  };

  const handleLogout = () => {
    setUser(null);
    setAccounts([]);
    setTransfer({ fromAccountNumber: '', toAccountNumber: '', amount: '' });
    writeLog('вы вышли из аккаунта');
  };

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    const response = await callApi(
      { method: 'post', url: '/accounts', data: { userId: user.userId, initialBalance: toKopecks(newAccountBalance) } },
      (account) => `счет ${account.accountNumber} создан, баланс ${(account.balance / 100).toFixed(2)} руб.`
    );
    if (!response) return;
    setNewAccountBalance('');
    await loadAccounts(user.userId);
  };

  const handleTransfer = async (e) => {
    e.preventDefault();
    const response = await callApi(
      {
        method: 'post',
        url: `/accounts/transfer?userId=${user.userId}`,
        data: { ...transfer, amount: toKopecks(transfer.amount) }
      },
      (result) => (typeof result === 'string' ? result : JSON.stringify(result))
    );
    if (!response) return;
    setTransfer({ fromAccountNumber: '', toAccountNumber: '', amount: '' });
    await loadAccounts(user.userId);
  };

  const handleDeleteAccount = async (accountId) => {
    const response = await callApi(
      { method: 'delete', url: `/accounts/${accountId}?userId=${user.userId}` },
      (result) => (typeof result === 'string' ? result : JSON.stringify(result))
    );
    if (response) await loadAccounts(user.userId);
  };

  const toggleMode = () => {
    setMode((prev) => (prev === 'login' ? 'register' : 'login'));
    setAuth({ username: '', password: '' });
  };

  return (
    <div className="App">
      <header className="hero">
        <h1>p2p transfer app</h1>
        <p>быстрые переводы между счетами без лишних шагов</p>
      </header>

      {!user ? (
        <section className="card auth-card">
          <h2>{mode === 'login' ? 'вход' : 'регистрация'}</h2>
          <form onSubmit={handleAuth} className="form">
            <input name="username" value={auth.username} onChange={updateAuth} placeholder="логин" required />
            <input name="password" type="password" value={auth.password} onChange={updateAuth} placeholder="пароль" required />
            <button type="submit">{mode === 'login' ? 'войти' : 'создать аккаунт'}</button>
          </form>
          <button className="link-btn" onClick={toggleMode}>
            {mode === 'login' ? 'нет аккаунта? зарегистрироваться' : 'уже есть аккаунт? войти'}
          </button>
        </section>
      ) : (
        <>
          <section className="user-bar card">
            <span>пользователь: <strong>{user.username}</strong></span>
            <button className="danger-btn" onClick={handleLogout}>выйти</button>
          </section>

          <section className="card">
            <h2>создать счет</h2>
            <form onSubmit={handleCreateAccount} className="form inline">
              <input
                name="initialBalance"
                type="number"
                step="0.01"
                min="0"
                value={newAccountBalance}
                onChange={(e) => setNewAccountBalance(e.target.value)}
                placeholder="начальный баланс, руб."
                required
              />
              <button type="submit">создать</button>
            </form>
          </section>

          <section className="card">
            <h2>перевод</h2>
            <form onSubmit={handleTransfer} className="form">
              <select name="fromAccountNumber" value={transfer.fromAccountNumber} onChange={updateTransfer} required>
                <option value="">с какого счета</option>
                {accounts.map((account) => (
                  <option key={account.id} value={account.accountNumber}>
                    {account.accountNumber} | {(account.balance / 100).toFixed(2)} руб.
                  </option>
                ))}
              </select>
              <input name="toAccountNumber" value={transfer.toAccountNumber} onChange={updateTransfer} placeholder="номер счета получателя" required />
              <input name="amount" type="number" step="0.01" min="0.01" value={transfer.amount} onChange={updateTransfer} placeholder="сумма, руб." required />
              <button type="submit">перевести</button>
            </form>
          </section>

          <section className="card">
            <h2>мои счета</h2>
            <div className="accounts-list">
              {accounts.length ? accounts.map((account) => (
                <article key={account.id} className="account-item">
                  <div className="account-meta">
                    <span>{account.accountNumber}</span>
                    <strong>{(account.balance / 100).toFixed(2)} руб.</strong>
                  </div>
                  <button className="danger-btn small" onClick={() => handleDeleteAccount(account.id)}>удалить</button>
                </article>
              )) : <p className="muted">у вас пока нет счетов</p>}
            </div>
          </section>
        </>
      )}

      <section className="card">
        <h2>история операций</h2>
        <div className="log-box">
          {logs.length ? logs.map((item, index) => (
            <div key={index} className={`log-message ${item.type}`}>{item.message}</div>
          )) : (
            <p className="muted">здесь будут ваши действия</p>
          )}
        </div>
      </section>
    </div>
  );
}

export default App;