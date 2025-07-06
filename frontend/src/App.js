import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

const apiClient = axios.create({
  baseURL: '/api',
});

function App() {
  const [currentUser, setCurrentUser] = useState(null);
  
  const [authMode, setAuthMode] = useState('login');
  const [authForm, setAuthForm] = useState({ username: '', password: '' });

  const [accForm, setAccForm] = useState({ initialBalance: '' });
  const [transferForm, setTransferForm] = useState({ fromAccountNumber: '', toAccountNumber: '', amount: '' });

  const [accounts, setAccounts] = useState([]);
  const [logMessages, setLogMessages] = useState([]);

  const log = (message, type = 'success', executionTime = null) => {
    const timestamp = new Date().toLocaleTimeString();
    const timeInfo = executionTime ? ` (${executionTime}мс)` : '';
    const fullMessage = `[${timestamp}] ${message}${timeInfo}`;
    setLogMessages(prev => [{ message: fullMessage, type }, ...prev]);
  };
  
  const handleApiError = (error) => {
    console.error('API Error:', error);
    
    if (error.response && error.response.data) {
      const errorData = error.response.data;
      let errorMessage;
      
      if (errorData.data && errorData.message) {
        // Новый формат API ответа
        errorMessage = errorData.message;
        log(errorMessage, 'error', errorData.executionTimeMs);
      } else if (typeof errorData === 'object') {
        errorMessage = JSON.stringify(errorData);
        log(errorMessage, 'error');
      } else {
        errorMessage = errorData;
        log(errorMessage, 'error');
      }
    } else if (error.message) {
      log(`Ошибка: ${error.message}`, 'error');
    } else {
      log('Произошла неизвестная ошибка', 'error');
    }
  };

  const handleInputChange = (setter) => (e) => {
    const { name, value } = e.target;
    setter(prev => ({ ...prev, [name]: value }));
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await apiClient.post('/users/login', authForm);
      const apiResponse = response.data;
      const user = apiResponse.data;
      
      setCurrentUser(user);
      log(`Добро пожаловать, ${user.username}!`, 'success', apiResponse.executionTimeMs);
      
      loadUserAccounts(user.userId);
    } catch (error) {
      handleApiError(error);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const response = await apiClient.post('/users/register', authForm);
      const apiResponse = response.data;
      const user = apiResponse.data;
      
      const normalizedUser = {
        userId: user.id,
        username: user.username
      };
      setCurrentUser(normalizedUser);
      log(`Регистрация успешна! Добро пожаловать, ${user.username}!`, 'success', apiResponse.executionTimeMs);
    } catch (error) {
      handleApiError(error);
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setAccounts([]);
    log('Вы вышли из системы.');
  };

  const loadUserAccounts = async (userId) => {
    try {
      const response = await apiClient.get(`/accounts/user/${userId}`);
      const apiResponse = response.data;
      
      setAccounts(apiResponse.data);
      log(`Счета успешно загружены.`, 'success', apiResponse.executionTimeMs);
    } catch (error) {
      handleApiError(error);
    }
  };

  const handleCreateAccount = async (e) => {
    e.preventDefault();
    try {
      const initialBalanceKopecks = Math.round(parseFloat(accForm.initialBalance) * 100);
      
      const response = await apiClient.post('/accounts', {
        userId: currentUser.userId,
        initialBalance: initialBalanceKopecks
      });
      
      const apiResponse = response.data;
      const account = apiResponse.data;
      
      log(`Счет создан: ${account.accountNumber} с балансом ${(account.balance / 100).toFixed(2)} руб.`, 'success', apiResponse.executionTimeMs);
      
      setAccForm({ initialBalance: '' });
      
      loadUserAccounts(currentUser.userId);
    } catch (error) {
      handleApiError(error);
    }
  };
  
  const handleTransfer = async (e) => {
    e.preventDefault();
    try {
      const amountKopecks = Math.round(parseFloat(transferForm.amount) * 100);
      
      const response = await apiClient.post(`/accounts/transfer?userId=${currentUser.userId}`, {
          fromAccountNumber: transferForm.fromAccountNumber,
          toAccountNumber: transferForm.toAccountNumber,
          amount: amountKopecks
      });
      
      const apiResponse = response.data;
      const message = typeof apiResponse.data === 'object' ? JSON.stringify(apiResponse.data) : apiResponse.data;
      
      log(message, 'success', apiResponse.executionTimeMs);
      
      setTransferForm({ fromAccountNumber: '', toAccountNumber: '', amount: '' });
      
      loadUserAccounts(currentUser.userId);
    } catch (error) {
      handleApiError(error);
    }
  };
  
  const handleDeleteAccount = async (accountId) => {
    try {
      const response = await apiClient.delete(`/accounts/${accountId}?userId=${currentUser.userId}`);
      const apiResponse = response.data;
      const message = typeof apiResponse.data === 'object' ? JSON.stringify(apiResponse.data) : apiResponse.data;
      
      log(message, 'success', apiResponse.executionTimeMs);
      
      loadUserAccounts(currentUser.userId);
    } catch (error) {
      handleApiError(error);
    }
  };

  const toggleAuthMode = () => {
    setAuthMode(authMode === 'login' ? 'register' : 'login');
    setAuthForm({ username: '', password: '' });
  };

  const inputStyle = {
    padding: '8px',
    marginBottom: '10px',
    width: '100%'
  };

  return (
    <div className="App">
      <h1>P2P-переводы</h1>
      
      {/* Authentication section - shown when user is not logged in */}
      {!currentUser ? (
        <div className="container auth-container">
          <h2>{authMode === 'login' ? 'Вход в систему' : 'Регистрация'}</h2>
          <form onSubmit={authMode === 'login' ? handleLogin : handleRegister}>
            <input 
              name="username" 
              value={authForm.username} 
              onChange={handleInputChange(setAuthForm)} 
              placeholder="Логин" 
              required 
              style={inputStyle}
            />
            <input 
              name="password" 
              type="password" 
              value={authForm.password} 
              onChange={handleInputChange(setAuthForm)} 
              placeholder="Пароль" 
              required 
              style={inputStyle}
            />
            <button type="submit">
              {authMode === 'login' ? 'Войти' : 'Зарегистрироваться'}
            </button>
          </form>
          <button 
            className="toggle-auth-btn" 
            onClick={toggleAuthMode}
            style={{ background: 'transparent', border: 'none', color: '#007bff', marginTop: '10px', cursor: 'pointer' }}
          >
            {authMode === 'login' ? 'Нет аккаунта? Зарегистрироваться' : 'Уже есть аккаунт? Войти'}
          </button>
        </div>
      ) : (
        <div>
          <div className="user-header" style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 20px', backgroundColor: '#f5f5f5', marginBottom: '20px', alignItems: 'center' }}>
            <span>Пользователь: <strong>{currentUser.username}</strong></span>
            <button onClick={handleLogout} style={{ background: '#dc3545', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '3px', cursor: 'pointer' }}>
              Выйти
            </button>
          </div>

          <div className="container">
            <h2>1. Создание счета</h2>
            <form onSubmit={handleCreateAccount}>
              <input 
                name="initialBalance" 
                type="number" 
                step="0.01"
                value={accForm.initialBalance} 
                onChange={handleInputChange(setAccForm)} 
                placeholder="Начальный баланс (в рублях)" 
                required 
                style={inputStyle}
              />
              <button type="submit">Создать счет</button>
            </form>
          </div>
          
          <div className="container">
            <h2>2. Перевод средств</h2>
            <form onSubmit={handleTransfer}>
              <select 
                name="fromAccountNumber" 
                value={transferForm.fromAccountNumber} 
                onChange={handleInputChange(setTransferForm)}
                required
                style={inputStyle}
              >
                <option value="">Выберите счет-источник</option>
                {accounts.map(acc => (
                  <option key={acc.id} value={acc.accountNumber}>
                    {acc.accountNumber} - Баланс: {(acc.balance / 100).toFixed(2)} руб.
                  </option>
                ))}
              </select>
              <input 
                name="toAccountNumber" 
                value={transferForm.toAccountNumber} 
                onChange={handleInputChange(setTransferForm)} 
                placeholder="Номер счета получателя" 
                required 
                style={inputStyle}
              />
              <input 
                name="amount" 
                type="number"
                step="0.01"
                value={transferForm.amount} 
                onChange={handleInputChange(setTransferForm)} 
                placeholder="Сумма (в рублях)" 
                required 
                style={inputStyle}
              />
              <button type="submit">Перевести</button>
            </form>
          </div>
          
          <div className="container">
            <h2>3. Мои счета</h2>
            <div className="accounts-list">
              {accounts.length === 0 ? (
                <p>У вас пока нет счетов. Создайте свой первый счет!</p>
              ) : (
                accounts.map(acc => (
                  <div key={acc.id} className="account-item">
                    <b>Счет:</b> {acc.accountNumber}, <b>Баланс:</b> {(acc.balance / 100).toFixed(2)} руб.
                    <button 
                      className="delete-btn" 
                      onClick={() => handleDeleteAccount(acc.id)}
                      style={{ marginLeft: '10px', background: '#ff6666', border: 'none', color: 'white', padding: '3px 8px', borderRadius: '3px', cursor: 'pointer' }}
                    >
                      Удалить счет
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      <div className="container">
        <h2>Лог операций:</h2>
        <div className="log-box">
          {logMessages.map((log, index) => (
            <div key={index} className={`log-message ${log.type}`}>
              {`> ${log.message}`}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default App;