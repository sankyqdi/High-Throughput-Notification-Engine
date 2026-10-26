import React, { useState, useEffect, useRef, useMemo } from 'react';
import {
  Mail,
  Users,
  Send,
  Star,
  Lock,
  Clock,
  ArrowRight,
  Activity,
  Server,
  Shield,
  Plus,
  Trash2,
  Zap,
  AlertTriangle,
  CheckCircle2,
  Cpu,
  Eye,
  EyeOff,
  Terminal,
  Play,
  Pause,
  Layers,
  User,
  Home,
  History,
  Settings,
  Copy,
  ChevronDown
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';

// ============================================================================
// КОНСТАНТЫ МАРШРУТОВ API
// ============================================================================
const API_V1 = "/api/v1";
const NOTIFICATIONS = `${API_V1}/notifications`;
const SEND_NOTIFICATION = "/send";
const GET_BY_ID = "/";
const API_REGISTER = `${API_V1}/senders`;
const USER = `${API_V1}/users`;
const AUTH = `${API_V1}/auth`;

const DEFAULT_API_URL = 'http://localhost:8080';
const QUEUE_CAPACITY = 5000;

const MESSAGE_TEMPLATES = [
  {
    id: 'silence',
    title: 'Тихое уведомление',
    tag: 'Личное',
    text: 'Иногда нужно просто написать. Сказать то, что давно откладывал, без лишнего шума и суеты. Это сообщение доставлено через защищённый многопоточный канал VoidMail.'
  },
  {
    id: 'release',
    title: 'Релиз ядра v2.4',
    tag: 'Система',
    text: 'Здравствуйте! Обновление системы асинхронных рассылок VoidMail Engine завершено. Пропускная способность пула потоков увеличена, все пароли SMTP защищены алгоритмом AES.'
  },
  {
    id: 'security',
    title: 'Контроль безопасности',
    tag: 'Безопасность',
    text: 'Зафиксирована авторизация в панели управления рассылками. Токен сессии верифицирован через Spring Security JWT.'
  }
];

export default function App() {
  const [activePage, setActivePage] = useState('home');
  const [profileTab, setProfileTab] = useState('senders');
  const [dispatchMode, setDispatchMode] = useState('single');

  const navigateTo = (page, subTab = null) => {
    setActivePage(page);
    if (page === 'profile' && subTab) setProfileTab(subTab);
    if (page === 'dispatch' && subTab) setDispatchMode(subTab);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const [apiBaseUrl, setApiBaseUrl] = useState(() => localStorage.getItem('htne_api_url') || DEFAULT_API_URL);
  const [isPolling, setIsPolling] = useState(true);

  const [token, setToken] = useState(() => localStorage.getItem('htne_jwt_token') || '');
  const [currentUser, setCurrentUser] = useState({
    id: null,
    email: 'Гость',
    role: 'ROLE_ANONYMOUS',
    enabled: false
  });
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [authTab, setAuthTab] = useState('login');
  const [authForm, setAuthForm] = useState({ email: '', password: '' });
  const [authLoading, setAuthLoading] = useState(false);

  // Метрики строго из бэкенда
  const [history, setHistory] = useState([]);
  const [liveStats, setLiveStats] = useState({
    currentQueueSize: 0,
    processCount: 0,
    errorsCount: 0,
    throughput: 0,
    queueCapacity: QUEUE_CAPACITY,
    addedInThisBatch: 0
  });

  const [sentHistory, setSentHistory] = useState([]);
  const [engineLogs, setEngineLogs] = useState([
    { id: 1, time: new Date().toLocaleTimeString('ru-RU'), type: 'INFO', msg: 'Инициализация сессии и опрос Spring Boot API' }
  ]);

  // Реальные почтовые ящики
  const [senders, setSenders] = useState([]);
  const [newSenderForm, setNewSenderForm] = useState({
    email: '',
    appPassword: '',
    host: 'smtp.yandex.ru',
    port: 465
  });

  const [selectedSenderId, setSelectedSenderId] = useState('');
  const [rawEmailsInput, setRawEmailsInput] = useState('');
  const [subjectInput, setSubjectInput] = useState('');
  const [messageBody, setMessageBody] = useState('');
  const [delayedSend, setDelayedSend] = useState(false);
  const [delayedTime, setDelayedTime] = useState('23:30');
  const [isSendingBatch, setIsSendingBatch] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' });

  const [toast, setToast] = useState(null);
  const showNotificationToast = (type, title, message) => {
    setToast({ type, title, message, id: Date.now() });
  };

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 5500);
    return () => clearTimeout(t);
  }, [toast]);

  const addLog = (type, msg) => {
    const time = new Date().toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    setEngineLogs((prev) => [{ id: Date.now() + Math.random(), time, type, msg }, ...prev.slice(0, 49)]);
  };

  const apiFetch = async (endpoint, options = {}) => {
    const headers = {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    };
    const response = await fetch(`${apiBaseUrl}${endpoint}`, { ...options, headers });
    if (!response.ok) {
      let errPayload = null;
      try { errPayload = await response.json(); }
      catch { errPayload = { status: response.status, message: `HTTP Ошибка ${response.status}` }; }
      throw errPayload;
    }
    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  };

  const fetchLiveBackendData = async () => {
    try {
      const [statsData, historyData, sendersData] = await Promise.all([
        apiFetch(`${NOTIFICATIONS}/stats`).catch(() => null),
        apiFetch(`${NOTIFICATIONS}/stats/history`).catch(() => null),
        apiFetch(API_REGISTER).catch(() => null)
      ]);

      if (statsData) {
        setLiveStats(statsData);
      }
      if (Array.isArray(historyData) && historyData.length > 0) {
        setHistory(
          historyData.map((pt) => ({
            ...pt,
            timeLabel: pt.dateTime ? new Date(pt.dateTime).toLocaleTimeString('ru-RU') : ''
          }))
        );
      }
      if (Array.isArray(sendersData)) {
        setSenders(sendersData);
        if (sendersData.length > 0 && !selectedSenderId) {
          setSelectedSenderId(sendersData[0].id);
        }
      }

      if (token) {
        const profileData = await apiFetch(`${USER}/me`).catch(() => null);
        if (profileData) setCurrentUser(profileData);
      }
    } catch (err) {
      addLog('ERROR', `Сервер недоступен: ${err.message || 'Ошибка сети'}`);
    }
  };

  useEffect(() => {
    fetchLiveBackendData();
  }, [apiBaseUrl, token]);

  useEffect(() => {
    if (!isPolling) return;
    const timer = setInterval(() => {
      fetchLiveBackendData();
    }, 4000);
    return () => clearInterval(timer);
  }, [isPolling, apiBaseUrl, token]);

  const parsedEmails = useMemo(() => {
    const rawList = rawEmailsInput.split(/[\n,;]+/).map((e) => e.trim()).filter(Boolean);
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const valid = [];
    const invalid = [];
    rawList.forEach((email) => {
      if (emailRegex.test(email)) valid.push(email);
      else invalid.push(email);
    });
    return { all: rawList, valid, invalid };
  }, [rawEmailsInput]);

  const handleSendNotificationBatch = async (e) => {
    if (e) e.preventDefault();
    if (!selectedSenderId) {
      showNotificationToast('error', 'Ошибка валидации', 'Выберите аккаунт отправителя');
      return;
    }
    if (parsedEmails.valid.length === 0) {
      showNotificationToast('error', 'Ошибка валидации', 'Укажите хотя бы один валидный email получателя');
      return;
    }
    if (!messageBody.trim()) {
      showNotificationToast('error', 'Ошибка валидации', 'Текст сообщения не может быть пустым');
      return;
    }

    setIsSendingBatch(true);
    const finalSubject = subjectInput.trim() || 'Новое уведомление';

    try {
      const stats = await apiFetch(`${NOTIFICATIONS}${SEND_NOTIFICATION}`, {
        method: 'POST',
        body: JSON.stringify({
          senderAccountId: Number(selectedSenderId),
          emails: parsedEmails.valid,
          subject: finalSubject,
          message: messageBody
        })
      });

      if (stats) setLiveStats(stats);
      addLog('SUCCESS', `POST ${NOTIFICATIONS}${SEND_NOTIFICATION}: ${parsedEmails.valid.length} писем отправлено в очередь`);
      showNotificationToast('success', 'Успешно отправлено', `Добавлено в очередь: ${stats?.addedInThisBatch || parsedEmails.valid.length}`);

      const currentSender = senders.find((s) => Number(s.id) === Number(selectedSenderId));
      setSentHistory((prev) => [
        {
          id: Date.now(),
          date: new Date().toLocaleTimeString('ru-RU'),
          sender: currentSender?.email || `ID #${selectedSenderId}`,
          recipientsCount: parsedEmails.valid.length,
          subject: finalSubject,
          preview: messageBody.slice(0, 100) + (messageBody.length > 100 ? '...' : ''),
          status: 'ОТПРАВЛЕНО'
        },
        ...prev
      ]);

      setMessageBody('');
      setSubjectInput('');
    } catch (err) {
      addLog('ERROR', `Сбой отправки: ${err.message || 'Ошибка сервера'}`);
      showNotificationToast('error', `Ошибка ${err.status || 500}`, err.message || 'Сбой отправки');
    } finally {
      setIsSendingBatch(false);
    }
  };

  const handleRegisterSender = async (e) => {
    e.preventDefault();
    if (!newSenderForm.email || !newSenderForm.appPassword) {
      showNotificationToast('error', 'Заполните поля', 'Email и пароль приложения обязательны');
      return;
    }
    try {
      await apiFetch(`${API_REGISTER}/register`, {
        method: 'POST',
        body: JSON.stringify({
          email: newSenderForm.email.trim(),
          appPassword: newSenderForm.appPassword,
          host: newSenderForm.host,
          port: Number(newSenderForm.port)
        })
      });
      await fetchLiveBackendData();
      addLog('SUCCESS', `Подключён новый SMTP-ящик: ${newSenderForm.email}`);
      showNotificationToast('success', 'Ящик сохранён', `Почта ${newSenderForm.email} привязана к аккаунту.`);
      setNewSenderForm({ email: '', appPassword: '', host: 'smtp.yandex.ru', port: 465 });
    } catch (err) {
      showNotificationToast('error', 'Ошибка сохранения', err.message);
    }
  };

  const handleDeleteSender = async (id, email) => {
    try {
      await apiFetch(`${API_REGISTER}${GET_BY_ID}${id}`, { method: 'DELETE' });
      await fetchLiveBackendData();
      addLog('INFO', `Удалена почта ${email}, кэш JavaMailSender очищен`);
      showNotificationToast('info', 'Почта удалена', `Кэш сессии сброшен.`);
    } catch (err) {
      showNotificationToast('error', 'Ошибка удаления', err.message);
    }
  };

  const handleAuthSubmit = async (e) => {
    e.preventDefault();
    setAuthLoading(true);
    try {
      const endpoint = authTab === 'register' ? `${AUTH}/register` : `${AUTH}/login`;
      const res = await apiFetch(endpoint, { method: 'POST', body: JSON.stringify(authForm) });
      if (res?.token) {
        localStorage.setItem('htne_jwt_token', res.token);
        setToken(res.token);
        setCurrentUser((prev) => ({ ...prev, email: res.email || authForm.email, enabled: true }));
        setShowAuthModal(false);
        addLog('SUCCESS', `Авторизован пользователь: ${authForm.email}`);
        await fetchLiveBackendData();
        showNotificationToast('success', 'Успешный вход', `Сессия активна`);
      } else if (authTab === 'register') {
        showNotificationToast('success', 'Аккаунт создан', 'Теперь выполните вход');
        setAuthTab('login');
      }
    } catch (err) {
      showNotificationToast('error', `Ошибка ${err.status || 401}`, err.message);
    } finally {
      setAuthLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    try {
      await apiFetch(`${USER}/change-password`, {
        method: 'PATCH',
        body: JSON.stringify(passwordForm)
      });
      showNotificationToast('success', 'Успешно', 'Пароль обновлён');
      setPasswordForm({ currentPassword: '', newPassword: '' });
    } catch (err) {
      showNotificationToast('error', 'Ошибка смены пароля', err.message);
    }
  };

  return (
    <div className="vb-k7d" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', position: 'relative' }}>
      {toast && (
        <div style={{ position: 'fixed', bottom: '24px', right: '24px', zIndex: 9999, maxWidth: '420px', width: '100%' }}>
          <div
            style={{
              padding: '16px',
              borderRadius: '16px',
              background: toast.type === 'error' ? 'rgba(35, 12, 28, 0.95)' : 'rgba(20, 14, 40, 0.95)',
              border: `1px solid ${toast.type === 'error' ? '#f43f5e' : '#a855f7'}`,
              boxShadow: '0 20px 40px rgba(0,0,0,0.6)',
              color: '#fff'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
              {toast.type === 'error' ? <AlertTriangle size={18} color="#f43f5e" /> : <CheckCircle2 size={18} color="#a855f7" />}
              <div style={{ flex: 1, fontSize: '13.5px' }}>
                <div style={{ fontWeight: 600 }}>{toast.title}</div>
                <div style={{ fontSize: '12.5px', color: '#b6a9db', marginTop: '4px' }}>{toast.message}</div>
              </div>
              <button onClick={() => setToast(null)} style={{ background: 'none', border: 'none', color: '#b6a9db', cursor: 'pointer' }}>✕</button>
            </div>
          </div>
        </div>
      )}

      {/* ШАПКА */}
      <header className="vb-k7d-nav">
        <a className="vb-k7d-logo" href="#home" onClick={(e) => { e.preventDefault(); navigateTo('home'); }}>
          <span className="vb-k7d-logo__mark" aria-hidden="true">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="1.9" strokeLinecap="round" strokeLinejoin="round"><path d="M22 2 11 13"/><path d="M22 2 15 22l-4-9-9-4 20-7z"/></svg>
          </span>
          VoidMail
        </a>

        <nav className="vb-k7d-menu" aria-label="Основная навигация">
          <a
            className={`vb-k7d-pill ${activePage === 'home' ? 'vb-k7d-pill--active' : ''}`}
            href="#home"
            onClick={(e) => { e.preventDefault(); navigateTo('home'); }}
          >
            <Home size={15} />
            Главная
          </a>
          <a
            className={`vb-k7d-pill ${activePage === 'dispatch' ? 'vb-k7d-pill--active' : ''}`}
            href="#dispatch"
            onClick={(e) => { e.preventDefault(); navigateTo('dispatch'); }}
          >
            <Send size={15} />
            Отправка и рассылка
          </a>
          <a
            className={`vb-k7d-pill ${activePage === 'telemetry' ? 'vb-k7d-pill--active' : ''}`}
            href="#telemetry"
            onClick={(e) => { e.preventDefault(); navigateTo('telemetry'); }}
          >
            <Activity size={15} />
            Мониторинг ({liveStats.currentQueueSize})
          </a>
          <a
            className={`vb-k7d-pill ${activePage === 'profile' ? 'vb-k7d-pill--active' : ''}`}
            href="#profile"
            onClick={(e) => { e.preventDefault(); navigateTo('profile', 'senders'); }}
          >
            <User size={15} />
            Профиль ({senders.length})
          </a>
        </nav>

        <div className="vb-k7d-navactions">
          <button
            onClick={() => { setAuthTab('login'); setShowAuthModal(true); }}
            className="t-btnflex t-btnflex_sm vb-k7d-btn vb-k7d-btn--ghost"
            style={{ cursor: 'pointer', padding: '9px 18px' }}
          >
            <span className="t-btnflex__text">{token ? currentUser.email : 'Войти'}</span>
          </button>
        </div>
      </header>

      {/* ====================================================================
          ЭКРАН 1: ГЛАВНАЯ СТРАНИЦА (ФОН НА ВЕСЬ ЭКРАН)
      ==================================================================== */}
      {activePage === 'home' && (
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', position: 'relative', overflow: 'hidden' }}>
          {/* СЛОИ ПОЛНОЭКРАННОГО АРТА */}
          <div className="vb-k7d-full-art" aria-hidden="true">
            <div className="vb-k7d-full-art__img" />
            <div className="vb-k7d-full-art__tint" />
            <div className="vb-k7d-full-art__fade" />
          </div>

          <div className="vb-k7d-inner">
            <div className="vb-k7d-copy">
              <p className="vb-k7d-badge">
                <span className="vb-k7d-badge__dot" aria-hidden="true" />
                Сообщения, которые доходят
              </p>

              <h1 className="vb-k7d-title" id="vb-k7d-title">
                Отправляй и рассылай сообщения <span className="vb-k7d-title__soft">без лишних эмоций</span>
              </h1>

              <p className="vb-k7d-lead">
                Иногда нужно просто написать. Сказать. Поделиться. Или разослать письма тысячам адресов через спокойный пул из 10 потоков — с динамическими темами, живой телеметрией и полным контролем каждого потока.
              </p>

              <div className="vb-k7d-actions">
                <button
                  onClick={() => navigateTo('dispatch')}
                  className="t-btnflex t-btnflex_md vb-k7d-btn vb-k7d-btn--primary"
                  style={{ border: 0, cursor: 'pointer', padding: '14px 28px' }}
                >
                  <span className="t-btnflex__text">Начать бесплатно</span>
                </button>
                <button
                  onClick={() => navigateTo('telemetry')}
                  className="t-btnflex t-btnflex_md vb-k7d-btn vb-k7d-btn--ghost"
                  style={{ cursor: 'pointer', padding: '14px 24px', display: 'inline-flex', alignItems: 'center', gap: '8px' }}
                >
                  <Activity size={16} />
                  <span className="t-btnflex__text">Открыть консоль</span>
                </button>
              </div>

              <ul className="vb-k7d-stats">
                <li className="vb-k7d-stat" onClick={() => navigateTo('dispatch')} style={{ cursor: 'pointer' }}>
                  <span className="vb-k7d-stat__top">
                    Отправка
                    <span className="vb-k7d-stat__ico"><Send size={15} /></span>
                  </span>
                  <span className="vb-k7d-stat__val">Конструктор</span>
                </li>
                <li className="vb-k7d-stat" onClick={() => navigateTo('profile', 'senders')} style={{ cursor: 'pointer' }}>
                  <span className="vb-k7d-stat__top">
                    Мои почты
                    <span className="vb-k7d-stat__ico"><Mail size={15} /></span>
                  </span>
                  <span className="vb-k7d-stat__val">{senders.length} активных</span>
                </li>
                <li className="vb-k7d-stat" onClick={() => navigateTo('telemetry')} style={{ cursor: 'pointer' }}>
                  <span className="vb-k7d-stat__top">
                    Очередь пула
                    <span className="vb-k7d-stat__ico"><Activity size={15} /></span>
                  </span>
                  <span className="vb-k7d-stat__val">{liveStats.currentQueueSize} / 5000</span>
                </li>
              </ul>
            </div>
          </div>

          {/* ВОЗМОЖНОСТИ */}
          <section className="vb-m3q">
            <div className="vb-m3q__wrap">
              <div className="vb-m3q__head">
                <h2 className="vb-m3q__title">Всё, что нужно,<br />чтобы письма доходили</h2>
                <p className="vb-m3q__subtitle">Четыре опоры VoidMail: скорость, очередь, личные почты и честная телеметрия по каждому потоку.</p>
              </div>

              <div className="vb-m3q__grid">
                <article className="vb-m3q-card" onClick={() => navigateTo('dispatch', 'single')} style={{ cursor: 'pointer' }}>
                  <span className="vb-m3q-card__icon"><Send size={20} /></span>
                  <h3 className="vb-m3q-card__title">Быстрая отправка</h3>
                  <p className="vb-m3q-card__text">Доставляйте одиночные письма с динамической темой и переменными в один клик — из конструктора или по API.</p>
                  <p className="vb-m3q-card__meta">Отклик API <strong>~180 мс</strong></p>
                </article>

                <article className="vb-m3q-card" onClick={() => navigateTo('dispatch', 'broadcast')} style={{ cursor: 'pointer' }}>
                  <span className="vb-m3q-card__icon"><Users size={20} /></span>
                  <h3 className="vb-m3q-card__title">Массовая рассылка</h3>
                  <p className="vb-m3q-card__text">Пакетная очередь до 5000 задач через пул из 10 потоков: паузы, повторы и мягкие лимиты без блокировок.</p>
                  <p className="vb-m3q-card__meta">Очередь <strong>{liveStats.currentQueueSize} / 5000</strong></p>
                </article>

                <article className="vb-m3q-card" onClick={() => navigateTo('profile', 'senders')} style={{ cursor: 'pointer' }}>
                  <span className="vb-m3q-card__icon"><Mail size={20} /></span>
                  <h3 className="vb-m3q-card__title">Личный профиль и SMTP</h3>
                  <p className="vb-m3q-card__text">Подключайте свои почты, ведите SMTP-контуры и смотрите полную историю отправок в профиле.</p>
                  <p className="vb-m3q-card__meta">Почтовых ящиков <strong>{senders.length} активных</strong></p>
                </article>

                <article className="vb-m3q-card" onClick={() => navigateTo('telemetry')} style={{ cursor: 'pointer' }}>
                  <span className="vb-m3q-card__icon"><Activity size={20} /></span>
                  <h3 className="vb-m3q-card__title">Живая телеметрия</h3>
                  <p className="vb-m3q-card__text">Графики пропускной способности, открытые и отказы — в реальном времени, с контролем каждого потока.</p>
                  <p className="vb-m3q-card__meta">Доставляемость <strong>99.2%</strong></p>
                </article>
              </div>
            </div>
          </section>

          {/* ПОДВАЛ */}
          <footer className="vb-w2n">
            <div className="vb-w2n__wrap">
              <div className="vb-w2n__top">
                <div>
                  <a className="vb-w2n__logo" href="#home" onClick={(e) => { e.preventDefault(); navigateTo('home'); }}>
                    <span className="vb-w2n__mark"><Send size={15} color="#fff" /></span>
                    VoidMail
                  </a>
                  <p className="vb-w2n__tag">Тихая многопоточная отправка писем: конструктор, очередь на 5000 задач и честная телеметрия.</p>
                </div>
                <div>
                  <p className="vb-w2n__coltitle">Продукт</p>
                  <ul className="vb-w2n__links">
                    <li><a href="#dispatch" onClick={(e) => { e.preventDefault(); navigateTo('dispatch'); }}>Отправка и рассылка</a></li>
                    <li><a href="#telemetry" onClick={(e) => { e.preventDefault(); navigateTo('telemetry'); }}>Мониторинг</a></li>
                    <li><a href="#profile" onClick={(e) => { e.preventDefault(); navigateTo('profile', 'senders'); }}>Мои почты</a></li>
                  </ul>
                </div>
                <div>
                  <p className="vb-w2n__coltitle">Разработчикам</p>
                  <ul className="vb-w2n__links">
                    <li><a href="#api" onClick={(e) => { e.preventDefault(); navigateTo('profile', 'security'); }}>REST API v1</a></li>
                    <li><a href="#jwt" onClick={(e) => { e.preventDefault(); navigateTo('profile', 'security'); }}>Spring Security JWT</a></li>
                  </ul>
                </div>
                <div>
                  <p className="vb-w2n__coltitle">Пользователь</p>
                  <ul className="vb-w2n__links">
                    <li><a href="#login" onClick={(e) => { e.preventDefault(); setShowAuthModal(true); }}>{currentUser.email}</a></li>
                    <li><a href="#history" onClick={(e) => { e.preventDefault(); navigateTo('profile', 'history'); }}>История кампаний</a></li>
                  </ul>
                </div>
              </div>
              <div className="vb-w2n__bottom">
                <span>© 2026 VoidMail. Все права защищены.</span>
                <span className="vb-w2n__status"><i aria-hidden="true" />Все системы работают стабильно</span>
              </div>
            </div>
          </footer>
        </div>
      )}

      {/* ====================================================================
          ЭКРАН 2: ОТПРАВКА И РАССЫЛКА
      ==================================================================== */}
      {activePage === 'dispatch' && (
        <section style={{ maxWidth: 1440, width: '100%', margin: '0 auto', padding: '36px clamp(16px, 4vw, 56px)' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.3fr) minmax(0, .7fr)', gap: '32px' }}>
            <form
              onSubmit={handleSendNotificationBatch}
              style={{
                background: 'linear-gradient(180deg, rgba(24,16,46,.96) 0%, rgba(14,9,28,.96) 100%)',
                border: '1px solid rgba(167,139,250,.24)',
                borderRadius: '22px',
                padding: '30px',
                display: 'grid',
                gap: '20px'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid rgba(167,139,250,.15)', paddingBottom: '16px' }}>
                <div>
                  <h2 style={{ margin: 0, fontSize: '20px', fontWeight: 700, color: '#fff' }}>
                    {dispatchMode === 'broadcast' ? 'Массовая рассылка' : 'Новое сообщение'}
                  </h2>
                  <p style={{ margin: '4px 0 0', fontSize: '13px', color: '#b6a9db' }}>
                    POST {NOTIFICATIONS}{SEND_NOTIFICATION} · ArrayBlockingQueue(5000)
                  </p>
                </div>
                <div style={{ display: 'flex', gap: '6px' }}>
                  <button
                    type="button"
                    onClick={() => setDispatchMode('single')}
                    className={`vb-k7d-pill ${dispatchMode === 'single' ? 'vb-k7d-pill--active' : ''}`}
                    style={{ padding: '6px 14px', fontSize: '12.5px', cursor: 'pointer' }}
                  >
                    Одиночное
                  </button>
                  <button
                    type="button"
                    onClick={() => setDispatchMode('broadcast')}
                    className={`vb-k7d-pill ${dispatchMode === 'broadcast' ? 'vb-k7d-pill--active' : ''}`}
                    style={{ padding: '6px 14px', fontSize: '12.5px', cursor: 'pointer' }}
                  >
                    Рассылка
                  </button>
                </div>
              </div>

              <div style={{ display: 'grid', gap: '6px' }}>
                <label className="vb-r5t-label">Почта отправителя (senderAccountId):</label>
                {senders.length === 0 ? (
                  <div style={{ padding: '12px', background: 'rgba(255,255,255,.03)', borderRadius: '12px', border: '1px dashed rgba(167,139,250,.3)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '13px', color: '#b6a9db' }}>Нет подключённых почт</span>
                    <button
                      type="button"
                      onClick={() => navigateTo('profile', 'senders')}
                      className="vb-k7d-pill"
                      style={{ padding: '6px 12px', fontSize: '12px', cursor: 'pointer' }}
                    >
                      + Подключить в профиле
                    </button>
                  </div>
                ) : (
                  <select
                    value={selectedSenderId}
                    onChange={(e) => setSelectedSenderId(Number(e.target.value))}
                    className="vb-r5t-input"
                    style={{ padding: '12px 14px', background: 'rgba(255,255,255,.05)' }}
                  >
                    {senders.map((s) => (
                      <option key={s.id} value={s.id} style={{ background: '#120c22', color: '#fff' }}>
                        {s.email} ({s.host}:{s.port})
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div style={{ display: 'grid', gap: '6px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <label className="vb-r5t-label">Получатели (emails):</label>
                  <span style={{ fontSize: '12px', color: '#a78bfa' }}>
                    Валидных: {parsedEmails.valid.length}
                  </span>
                </div>
                {dispatchMode === 'broadcast' ? (
                  <textarea
                    rows={4}
                    value={rawEmailsInput}
                    onChange={(e) => setRawEmailsInput(e.target.value)}
                    placeholder="Введите email адреса через запятую или с новой строки..."
                    className="vb-r5t-input"
                    style={{ resize: 'vertical' }}
                  />
                ) : (
                  <input
                    type="text"
                    value={rawEmailsInput}
                    onChange={(e) => setRawEmailsInput(e.target.value)}
                    placeholder="name@company.ru"
                    className="vb-r5t-input"
                  />
                )}
              </div>

              <div style={{ display: 'grid', gap: '6px' }}>
                <label className="vb-r5t-label">Тема письма (subject):</label>
                <input
                  type="text"
                  value={subjectInput}
                  onChange={(e) => setSubjectInput(e.target.value)}
                  placeholder="Новое уведомление (если пусто, подставится по умолчанию)"
                  className="vb-r5t-input"
                />
              </div>

              <div style={{ display: 'grid', gap: '6px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <label className="vb-r5t-label">Сообщение (message):</label>
                  <span style={{ fontSize: '12px', color: messageBody.length > 1000 ? '#f43f5e' : '#b6a9db' }}>
                    {messageBody.length} / 1000
                  </span>
                </div>
                <textarea
                  rows={6}
                  value={messageBody}
                  onChange={(e) => setMessageBody(e.target.value)}
                  placeholder="Напишите текст вашего уведомления..."
                  className="vb-r5t-input"
                  style={{ resize: 'none' }}
                />
              </div>

              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: '10px' }}>
                <span style={{ fontSize: '12.5px', color: '#b6a9db' }}>
                  Мгновенная доставка в пул воркеров
                </span>
                <button
                  type="submit"
                  disabled={isSendingBatch}
                  className="t-btnflex t-btnflex_md vb-k7d-btn vb-k7d-btn--primary"
                  style={{ border: 0, padding: '14px 28px', cursor: 'pointer', opacity: isSendingBatch ? .6 : 1 }}
                >
                  <span className="t-btnflex__text">
                    {isSendingBatch ? 'Постановка в очередь...' : `Отправить (${parsedEmails.valid.length})`}
                  </span>
                </button>
              </div>
            </form>

            <aside style={{ display: 'grid', gap: '18px' }}>
              <div style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.18)', borderRadius: '18px', padding: '20px' }}>
                <h3 style={{ margin: '0 0 12px', fontSize: '15px', color: '#fff' }}>Шаблоны писем</h3>
                <div style={{ display: 'grid', gap: '10px' }}>
                  {MESSAGE_TEMPLATES.map((t) => (
                    <button
                      key={t.id}
                      type="button"
                      onClick={() => { setSubjectInput(t.title); setMessageBody(t.text); }}
                      style={{
                        padding: '12px',
                        background: 'rgba(255,255,255,.03)',
                        border: '1px solid rgba(167,139,250,.15)',
                        borderRadius: '12px',
                        textAlign: 'left',
                        color: '#dcd3ff',
                        cursor: 'pointer'
                      }}
                    >
                      <div style={{ fontWeight: 600, fontSize: '13.5px' }}>{t.title}</div>
                      <div style={{ fontSize: '11.5px', color: '#b6a9db', marginTop: '4px' }}>{t.text.slice(0, 75)}...</div>
                    </button>
                  ))}
                </div>
              </div>
            </aside>
          </div>
        </section>
      )}

      {/* ====================================================================
          ЭКРАН 3: МОНИТОРИНГ
      ==================================================================== */}
      {activePage === 'telemetry' && (
        <section style={{ maxWidth: 1440, width: '100%', margin: '0 auto', padding: '36px clamp(16px, 4vw, 56px)' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, minmax(0, 1fr))', gap: '14px', marginBottom: '24px' }}>
            <div className="vb-k7d-stat">
              <span className="vb-k7d-stat__top">Очередь задач <Layers size={15} /></span>
              <span className="vb-k7d-stat__val">{liveStats.currentQueueSize} / 5000</span>
            </div>
            <div className="vb-k7d-stat">
              <span className="vb-k7d-stat__top">Успешно доставлено <CheckCircle2 size={15} /></span>
              <span className="vb-k7d-stat__val">{liveStats.processCount}</span>
            </div>
            <div className="vb-k7d-stat">
              <span className="vb-k7d-stat__top">Throughput <Activity size={15} /></span>
              <span className="vb-k7d-stat__val">{liveStats.throughput} / 5с</span>
            </div>
            <div className="vb-k7d-stat">
              <span className="vb-k7d-stat__top">Ошибки доставки <AlertTriangle size={15} /></span>
              <span className="vb-k7d-stat__val" style={{ color: '#f43f5e' }}>{liveStats.errorsCount}</span>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.25fr) minmax(0, .75fr)', gap: '28px' }}>
            <div style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.2)', borderRadius: '22px', padding: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h3 style={{ margin: 0, fontSize: '17px', color: '#fff' }}>Динамика очереди и Throughput</h3>
                <button
                  onClick={() => setIsPolling(!isPolling)}
                  className="vb-k7d-pill"
                  style={{ border: '1px solid rgba(167,139,250,.2)', padding: '6px 12px', fontSize: '12px', cursor: 'pointer' }}
                >
                  {isPolling ? <Pause size={13} /> : <Play size={13} />}
                  {isPolling ? 'Пауза' : 'Пуск'}
                </button>
              </div>

              <div style={{ height: 320, width: '100%' }}>
                {history.length === 0 ? (
                  <div style={{ height: '100%', display: 'grid', placeItems: 'center', color: '#8f83b6', fontSize: '13px' }}>
                    Ожидание данных от Spring Boot (/stats/history)...
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={history} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                      <defs>
                        <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#7c5cff" stopOpacity={0.6} />
                          <stop offset="95%" stopColor="#7c5cff" stopOpacity={0.0} />
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" stroke="rgba(167,139,250,.1)" vertical={false} />
                      <XAxis dataKey="timeLabel" stroke="#8f83b6" fontSize={11} />
                      <YAxis stroke="#8f83b6" fontSize={11} />
                      <Tooltip contentStyle={{ background: '#120c22', borderColor: '#a855f7', borderRadius: '12px' }} />
                      <Area type="monotone" dataKey="throughput" stroke="#a855f7" fill="url(#chartGrad)" />
                      <Area type="monotone" dataKey="currentQueueSize" stroke="#6ee7a8" fillOpacity={0.1} />
                    </AreaChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>

            {/* УВЕЛИЧЕННЫЙ ЖУРНАЛ ЛОГОВ */}
            <div style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.2)', borderRadius: '22px', padding: '24px', display: 'flex', flexDirection: 'column' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                <h3 style={{ margin: 0, fontSize: '16px', color: '#fff', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Terminal size={18} color="#a855f7" />
                  Журнал логов ядра
                </h3>
                <button
                  onClick={() => setEngineLogs([])}
                  style={{ background: 'none', border: 'none', color: '#b6a9db', cursor: 'pointer', fontSize: '12px' }}
                >
                  Очистить
                </button>
              </div>

              <div
                style={{
                  flex: 1,
                  minHeight: '340px',
                  maxHeight: '380px',
                  overflowY: 'auto',
                  background: 'rgba(8,5,18,.85)',
                  border: '1px solid rgba(167,139,250,.18)',
                  borderRadius: '16px',
                  padding: '16px',
                  fontSize: '12.5px',
                  fontFamily: 'monospace',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '8px'
                }}
              >
                {engineLogs.length === 0 ? (
                  <div style={{ color: '#685e87', textAlign: 'center', marginTop: 'auto', marginBottom: 'auto' }}>
                    Журнал пуст
                  </div>
                ) : (
                  engineLogs.map((l) => (
                    <div key={l.id} style={{ lineHeight: '1.5', borderBottom: '1px solid rgba(255,255,255,.04)', paddingBottom: '4px' }}>
                      <span style={{ color: '#8f83b6' }}>[{l.time}]</span>{' '}
                      <span style={{ color: l.type === 'ERROR' ? '#f43f5e' : l.type === 'SUCCESS' ? '#6ee7a8' : '#c084fc', fontWeight: 600 }}>
                        {l.type}:
                      </span>{' '}
                      <span style={{ color: '#e2ddfa' }}>{l.msg}</span>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </section>
      )}

      {/* ====================================================================
          ЭКРАН 4: ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ
      ==================================================================== */}
      {activePage === 'profile' && (
        <section style={{ maxWidth: 1440, width: '100%', margin: '0 auto', padding: '36px clamp(16px, 4vw, 56px)' }}>
          <div style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.25)', borderRadius: '22px', padding: '24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px', marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <div style={{ width: 48, height: 48, borderRadius: '14px', background: 'linear-gradient(135deg, #7c5cff, #a855f7)', display: 'grid', placeItems: 'center', fontSize: '18px', fontWeight: 700, color: '#fff' }}>
                {currentUser.email ? currentUser.email[0]?.toUpperCase() : 'U'}
              </div>
              <div>
                <h2 style={{ margin: 0, fontSize: '18px', color: '#fff' }}>{currentUser.email}</h2>
                <p style={{ margin: '2px 0 0', fontSize: '13px', color: '#b6a9db' }}>Личный кабинет отправителя</p>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                onClick={() => setProfileTab('senders')}
                className={`vb-k7d-pill ${profileTab === 'senders' ? 'vb-k7d-pill--active' : ''}`}
                style={{ cursor: 'pointer' }}
              >
                Мои почты ({senders.length})
              </button>
              <button
                onClick={() => setProfileTab('history')}
                className={`vb-k7d-pill ${profileTab === 'history' ? 'vb-k7d-pill--active' : ''}`}
                style={{ cursor: 'pointer' }}
              >
                История отправок ({sentHistory.length})
              </button>
              <button
                onClick={() => setProfileTab('security')}
                className={`vb-k7d-pill ${profileTab === 'security' ? 'vb-k7d-pill--active' : ''}`}
                style={{ cursor: 'pointer' }}
              >
                Безопасность
              </button>
            </div>
          </div>

          {profileTab === 'senders' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.25fr) minmax(0, .75fr)', gap: '28px' }}>
              <div style={{ display: 'grid', gap: '12px' }}>
                {senders.length === 0 ? (
                  <div style={{ background: '#120c22', border: '1px dashed rgba(167,139,250,.2)', borderRadius: '16px', padding: '32px', textAlign: 'center', color: '#b6a9db', fontSize: '14px' }}>
                    У вас пока нет зарегистрированных SMTP-ящиков. Добавьте первый в форме справа.
                  </div>
                ) : (
                  senders.map((s) => (
                    <div key={s.id} style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.2)', borderRadius: '16px', padding: '16px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <div>
                        <div style={{ fontWeight: 600, color: '#fff', fontSize: '15px' }}>{s.email}</div>
                        <div style={{ fontSize: '12px', color: '#8f83b6', marginTop: '2px' }}>{s.host}:{s.port} · Шифрование AES-128</div>
                      </div>
                      <div style={{ display: 'flex', gap: '10px' }}>
                        <button
                          onClick={() => { setSelectedSenderId(s.id); navigateTo('dispatch'); }}
                          className="vb-k7d-pill"
                          style={{ border: '1px solid rgba(167,139,250,.3)', fontSize: '12px', cursor: 'pointer' }}
                        >
                          Выбрать для рассылки
                        </button>
                        <button
                          onClick={() => handleDeleteSender(s.id, s.email)}
                          style={{ background: 'none', border: 'none', color: '#f43f5e', cursor: 'pointer', padding: '8px' }}
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>

              <form onSubmit={handleRegisterSender} className="vb-r5t-form" style={{ background: '#120c22' }}>
                <h3 style={{ margin: 0, fontSize: '16px', color: '#fff' }}>+ Подключить Яндекс SMTP</h3>
                <div className="vb-r5t-group">
                  <label className="vb-r5t-label">Email ящика:</label>
                  <input
                    type="email"
                    required
                    value={newSenderForm.email}
                    onChange={(e) => setNewSenderForm({ ...newSenderForm, email: e.target.value })}
                    placeholder="account@yandex.ru"
                    className="vb-r5t-input"
                  />
                </div>
                <div className="vb-r5t-group">
                  <label className="vb-r5t-label">Пароль приложения:</label>
                  <input
                    type="password"
                    required
                    value={newSenderForm.appPassword}
                    onChange={(e) => setNewSenderForm({ ...newSenderForm, appPassword: e.target.value })}
                    placeholder="••••••••••••••••"
                    className="vb-r5t-input"
                  />
                </div>
                <button type="submit" className="t-submit t-btnflex t-btnflex_md" style={{ padding: '12px' }}>
                  <span className="t-btnflex__text">Зашифровать и привязать</span>
                </button>
              </form>
            </div>
          )}

          {profileTab === 'history' && (
            <div style={{ display: 'grid', gap: '12px' }}>
              {sentHistory.length === 0 ? (
                <div style={{ background: '#120c22', border: '1px dashed rgba(167,139,250,.2)', borderRadius: '16px', padding: '32px', textAlign: 'center', color: '#b6a9db', fontSize: '14px' }}>
                  История отправок в этой сессии пуста
                </div>
              ) : (
                sentHistory.map((h) => (
                  <div key={h.id} style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.2)', borderRadius: '16px', padding: '18px 22px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div style={{ fontWeight: 600, color: '#fff', fontSize: '15px' }}>{h.subject}</div>
                      <span style={{ fontSize: '11px', padding: '4px 8px', borderRadius: '6px', background: 'rgba(110,231,168,.14)', color: '#86efc0' }}>
                        {h.status}
                      </span>
                    </div>
                    <p style={{ margin: '8px 0', fontSize: '13px', color: '#b6a9db' }}>{h.preview}</p>
                    <div style={{ fontSize: '12px', color: '#8f83b6' }}>
                      От: {h.sender} · Получателей: {h.recipientsCount} · {h.date}
                    </div>
                  </div>
                ))
              )}
            </div>
          )}

          {profileTab === 'security' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, 1fr)', gap: '24px' }}>
              <form onSubmit={handleChangePassword} style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.2)', borderRadius: '22px', padding: '24px' }}>
                <h3 style={{ margin: '0 0 16px', fontSize: '16px', color: '#fff' }}>Смена пароля (PATCH {USER}/change-password)</h3>
                <div className="vb-r5t-group">
                  <label className="vb-r5t-label">Текущий пароль:</label>
                  <input
                    type="password"
                    required
                    value={passwordForm.currentPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                    className="vb-r5t-input"
                  />
                </div>
                <div className="vb-r5t-group" style={{ marginTop: '12px' }}>
                  <label className="vb-r5t-label">Новый пароль:</label>
                  <input
                    type="password"
                    required
                    value={passwordForm.newPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                    className="vb-r5t-input"
                  />
                </div>
                <button type="submit" className="t-submit t-btnflex t-btnflex_md" style={{ marginTop: '16px', padding: '12px' }}>
                  <span className="t-btnflex__text">Обновить пароль</span>
                </button>
              </form>

              <div style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.2)', borderRadius: '22px', padding: '24px' }}>
                <h3 style={{ margin: '0 0 16px', fontSize: '16px', color: '#fff' }}>Spring Boot Подключение</h3>
                <div className="vb-r5t-group">
                  <label className="vb-r5t-label">URL сервера:</label>
                  <input
                    type="text"
                    value={apiBaseUrl}
                    onChange={(e) => {
                      setApiBaseUrl(e.target.value);
                      localStorage.setItem('htne_api_url', e.target.value);
                    }}
                    className="vb-r5t-input"
                  />
                </div>
                <div style={{ marginTop: '14px', fontSize: '12px', color: '#8f83b6', wordBreak: 'break-all' }}>
                  JWT Токен: {token ? `${token.slice(0, 36)}...` : 'Не авторизован'}
                </div>
              </div>
            </div>
          )}
        </section>
      )}

      {/* МОДАЛЬНОЕ ОКНО АВТОРИЗАЦИИ */}
      {showAuthModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,.75)', backdropFilter: 'blur(6px)', display: 'grid', placeItems: 'center', zIndex: 9999 }}>
          <div style={{ background: '#120c22', border: '1px solid rgba(167,139,250,.3)', borderRadius: '20px', padding: '30px', maxWidth: '420px', width: '90%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ margin: 0, color: '#fff', fontSize: '18px' }}>VoidMail Вход / Регистрация</h3>
              <button onClick={() => setShowAuthModal(false)} style={{ background: 'none', border: 'none', color: '#b6a9db', cursor: 'pointer' }}>✕</button>
            </div>
            <form onSubmit={handleAuthSubmit} style={{ display: 'grid', gap: '14px' }}>
              <div className="vb-r5t-group">
                <label className="vb-r5t-label">Email:</label>
                <input
                  type="email"
                  required
                  value={authForm.email}
                  onChange={(e) => setAuthForm({ ...authForm, email: e.target.value })}
                  placeholder="name@company.ru"
                  className="vb-r5t-input"
                />
              </div>
              <div className="vb-r5t-group">
                <label className="vb-r5t-label">Пароль:</label>
                <input
                  type="password"
                  required
                  value={authForm.password}
                  onChange={(e) => setAuthForm({ ...authForm, password: e.target.value })}
                  placeholder="••••••••"
                  className="vb-r5t-input"
                />
              </div>
              <button type="submit" disabled={authLoading} className="t-submit t-btnflex t-btnflex_md" style={{ padding: '12px', marginTop: '6px' }}>
                <span className="t-btnflex__text">
                  {authLoading ? 'Проверка...' : authTab === 'login' ? 'Войти по JWT' : 'Создать аккаунт'}
                </span>
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}