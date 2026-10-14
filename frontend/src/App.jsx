import { useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import {
  ArrowRight,
  BarChart3,
  Briefcase,
  CheckCircle2,
  Download,
  FileText,
  LogOut,
  ShieldCheck,
  Sparkles,
  Upload,
  Wand2,
  XCircle,
} from 'lucide-react'
import { BrowserRouter, Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import './App.css'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1'
const DEFAULT_JOB_DESCRIPTION = ''

const api = axios.create({
  baseURL: API_BASE,
})

function getAuthConfig(token) {
  return token ? { headers: { Authorization: `Bearer ${token}` } } : {}
}

function App() {
  const [auth, setAuth] = useState(() => {
    try {
      const saved = localStorage.getItem('resumeiq-auth')
      return saved ? JSON.parse(saved) : null
    } catch {
      return null
    }
  })

  // Global 401 interceptor — clears stale tokens instantly for ALL requests,
  // preventing a flood of auth errors when the backend restarts or the token expires.
  useEffect(() => {
    const interceptorId = api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          setAuth(null)
        }
        return Promise.reject(error)
      },
    )
    return () => api.interceptors.response.eject(interceptorId)
  }, [])

  useEffect(() => {
    if (auth?.token) {
      api.defaults.headers.common.Authorization = `Bearer ${auth.token}`
      localStorage.setItem('resumeiq-auth', JSON.stringify(auth))
    } else {
      delete api.defaults.headers.common.Authorization
      localStorage.removeItem('resumeiq-auth')
    }
  }, [auth])

  useEffect(() => {
    if (!auth?.token) return

    api
      .get('/auth/me')
      .then((response) => {
        setAuth((current) => (current ? { ...current, user: response.data } : null))
      })
      .catch(() => {
        // 401 is already handled by the interceptor above; this handles other errors.
        setAuth(null)
      })
  }, [auth?.token])

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage auth={auth} />} />
        <Route
          path="/auth"
          element={
            auth?.token ? (
              <Navigate to="/dashboard" replace />
            ) : (
              <AuthPage setAuth={setAuth} />
            )
          }
        />
        <Route
          path="/dashboard"
          element={
            auth?.token ? (
              <DashboardPage auth={auth} setAuth={setAuth} />
            ) : (
              <Navigate to="/auth" replace />
            )
          }
        />
      </Routes>
    </BrowserRouter>
  )
}

function LandingPage({ auth }) {
  return (
    <div className="page-shell">
      <div style={{ background: 'var(--surface)', borderBottom: '1px solid var(--border)', position: 'sticky', top: 0, zIndex: 50 }}>
        <header className="topbar" style={{ borderBottom: 'none', position: 'static' }}>
          <div className="brand-wrap">
            <div className="brand-mark">
              <Sparkles size={18} />
            </div>
            <span>ResumeIQ</span>
          </div>
          <nav className="nav-links">
            <a href="#features">Features</a>
            <a href="#workflow">Workflow</a>
            <a href="#metrics">Results</a>
          </nav>
          {auth?.token ? (
            <Link className="primary-btn small" to="/dashboard">
              Dashboard
            </Link>
          ) : (
            <Link className="primary-btn small" to="/auth">
              Start free
            </Link>
          )}
        </header>
      </div>

      <main className="landing-page">
        <section className="hero-section">
          <div className="hero-copy">
            <span className="eyebrow">AI-powered resume intelligence</span>
            <h1>Turn every resume into a stronger, ATS-ready application.</h1>
            <p>
              Analyze job fit, surface keyword gaps, generate polished resume variants, and
              keep a clear record of every improvement before you hit apply.
            </p>
            <div className="hero-actions">
              <Link className="primary-btn" to={auth?.token ? '/dashboard' : '/auth'}>
                {auth?.token ? 'Go to dashboard' : 'Create account'}
                <ArrowRight size={18} />
              </Link>
              <a className="secondary-btn" href="#features">
                Explore features
              </a>
            </div>
            <div className="hero-stats" id="metrics">
              <div>
                <strong>ATS</strong>
                <span>Transparent match insights</span>
              </div>
              <div>
                <strong>AI</strong>
                <span>Role-specific feedback</span>
              </div>
              <div>
                <strong>DOCX</strong>
                <span>Download improved resume</span>
              </div>
            </div>
          </div>

          <div className="hero-panel">
            <div className="mini-card accent-card">
              <div className="mini-header">
                <ShieldCheck size={18} />
                <span>ATS signals</span>
              </div>
              <div className="score-ring">
                <strong>--</strong>
                <span>score</span>
              </div>
              <ul>
                <li>Matched and missing skills from your resume</li>
                <li>Recommendations based on your target role</li>
              </ul>
            </div>
            <div className="mini-card">
              <div className="mini-header">
                <BarChart3 size={18} />
                <span>Role match</span>
              </div>
              <div className="score-bar">
                <span style={{ width: '0%' }} />
              </div>
              <p>Upload a resume to calculate your actual role match.</p>
            </div>
          </div>
        </section>

        <section className="feature-grid" id="features">
          <div className="feature-card">
            <FileText size={24} />
            <h3>Resume parsing</h3>
            <p>Upload PDF or DOCX files and extract structured experience, skills, and strengths.</p>
          </div>
          <div className="feature-card">
            <Briefcase size={24} />
            <h3>Job fit analysis</h3>
            <p>Compare resume content with target roles and identify unmatched requirements.</p>
          </div>
          <div className="feature-card">
            <Wand2 size={24} />
            <h3>AI resume rewrite</h3>
            <p>Generate cleaner summaries and ATS-friendly resumes tailored to each job.</p>
          </div>
        </section>

        <section className="workflow" id="workflow">
          <div className="section-header">
            <span className="eyebrow">How it works</span>
            <h2>From raw resume to optimized application in three steps</h2>
          </div>
          <div className="workflow-grid">
            <div className="workflow-step">
              <span>01</span>
              <h3>Upload</h3>
              <p>Drop in a resume and let the parser extract core content.</p>
            </div>
            <div className="workflow-step">
              <span>02</span>
              <h3>Analyze</h3>
              <p>Check ATS score, missing keywords, skills alignment, and gaps.</p>
            </div>
            <div className="workflow-step">
              <span>03</span>
              <h3>Improve</h3>
              <p>Generate a better version and download the optimized document.</p>
            </div>
          </div>
        </section>
      </main>
    </div>
  )
}

function AuthPage({ setAuth }) {
  const navigate = useNavigate()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')

    try {
      const endpoint = mode === 'login' ? '/auth/login' : '/auth/register'
      const payload =
        mode === 'login'
          ? { email: form.email, password: form.password }
          : {
              fullName: form.fullName,
              email: form.email,
              password: form.password,
              confirmPassword: form.confirmPassword,
            }

      const response = await api.post(endpoint, payload)
      setAuth({ token: response.data.token, user: response.data.user })
      navigate('/dashboard')
    } catch (requestError) {
      const message = requestError.response?.data?.message || requestError.response?.data?.error || 'Authentication failed.'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-shell">
        <div className="auth-copy">
          <div className="brand-wrap">
            <div className="brand-mark">
              <Sparkles size={18} />
            </div>
            <span>ResumeIQ</span>
          </div>
          <h1>Grow your chances with smarter resume decisions.</h1>
          <p>
            Screen for ATS fit, improve keyword coverage, and generate polished resumes aligned with the roles you target.
          </p>
        </div>

        <div className="auth-card">
          <div className="mode-switcher">
            <button
              type="button"
              className={mode === 'login' ? 'active' : ''}
              onClick={() => setMode('login')}
            >
              Login
            </button>
            <button
              type="button"
              className={mode === 'register' ? 'active' : ''}
              onClick={() => setMode('register')}
            >
              Register
            </button>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            {mode === 'register' && (
              <label>
                <span>Full name</span>
                <input
                  type="text"
                  name="fullName"
                  value={form.fullName}
                  onChange={handleChange}
                  placeholder="Alex Morgan"
                  required
                />
              </label>
            )}

            <label>
              <span>Email</span>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="you@example.com"
                required
              />
            </label>

            <label>
              <span>Password</span>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="Password123!"
                required
              />
            </label>

            {mode === 'register' && (
              <label>
                <span>Confirm password</span>
                <input
                  type="password"
                  name="confirmPassword"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="Repeat password"
                  required
                />
              </label>
            )}

            {error && <p className="form-error">{error}</p>}

            <button type="submit" className="primary-btn full-width" disabled={loading}>
              {loading ? 'Please wait...' : mode === 'login' ? 'Sign in' : 'Create account'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

function DashboardPage({ auth, setAuth }) {
  const [resumes, setResumes] = useState([])
  const [analyses, setAnalyses] = useState([])
  const [generatedResumes, setGeneratedResumes] = useState([])
  const [editingGeneratedId, setEditingGeneratedId] = useState('')
  const [editingContent, setEditingContent] = useState('')
  const [savingGenerated, setSavingGenerated] = useState(false)
  const [busy, setBusy] = useState(false)
  const [selectedResumeId, setSelectedResumeId] = useState('')
  const [selectedAnalysisId, setSelectedAnalysisId] = useState('')
  const [jobDescription, setJobDescription] = useState(DEFAULT_JOB_DESCRIPTION)
  const [loading, setLoading] = useState(true)
  const [notice, setNotice] = useState('')

  const selectedResume = useMemo(
    () => resumes.find((resume) => String(resume.id) === String(selectedResumeId)) || resumes[0] || null,
    [resumes, selectedResumeId],
  )

  const selectedAnalysis = useMemo(() => {
    const forResume = analyses.filter((analysis) => String(analysis.resumeId) === String(selectedResume?.id))
    return forResume.find((analysis) => String(analysis.id) === String(selectedAnalysisId)) || forResume[0] || null
  }, [analyses, selectedAnalysisId, selectedResume?.id])

  const loadDashboard = async () => {
    try {
      const [resumeResponse, analysisResponse, generatedResponse] = await Promise.all([
        api.get('/resumes', getAuthConfig(auth.token)),
        api.get('/analyses', getAuthConfig(auth.token)),
        api.get('/generated-resumes', getAuthConfig(auth.token)),
      ])

      const loadedResumes = resumeResponse.data || []
      const loadedAnalyses = analysisResponse.data || []
      const loadedGenerated = generatedResponse.data || []

      setResumes(loadedResumes)
      setAnalyses(loadedAnalyses)
      setGeneratedResumes(loadedGenerated)

      if (loadedResumes.length > 0 && !selectedResumeId) {
        setSelectedResumeId(String(loadedResumes[0].id))
      }

      if (loadedAnalyses.length > 0 && !selectedAnalysisId) {
        setSelectedAnalysisId(String(loadedAnalyses[0].id))
      }
    } catch (error) {
      setNotice(error.response?.data?.message || 'Unable to load dashboard data.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDashboard()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [auth.token])

  const handleUpload = async (event) => {
    const file = event.target.files?.[0]
    if (!file) return

    const allowed = /\.(pdf|docx)$/i.test(file.name)
    if (!allowed) {
      setNotice('Please upload a PDF or DOCX file.')
      event.target.value = ''
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      setNotice('The maximum file size is 10 MB.')
      event.target.value = ''
      return
    }
    const formData = new FormData()
    formData.append('file', file)
    setBusy(true)

    try {
      await api.post('/resumes', formData, {
        headers: {
          ...getAuthConfig(auth.token).headers,
        },
      })
      event.target.value = ''
      setNotice(`${file.name} uploaded successfully.`)
      await loadDashboard()
    } catch (error) {
      setNotice(error.response?.data?.message || 'Resume upload failed.')
    } finally {
      setBusy(false)
    }
  }

  const handleAnalyze = async () => {
    if (!selectedResume?.id) {
      setNotice('Choose a resume before running analysis.')
      return
    }

    setBusy(true)
    try {
      const response = await api.post(`/analyses/general?resumeId=${selectedResume.id}`, { jobDescription }, getAuthConfig(auth.token))
      const nextAnalyses = [response.data, ...analyses.filter((entry) => entry.id !== response.data.id)]
      setAnalyses(nextAnalyses)
      setSelectedAnalysisId(String(response.data.id))
      setNotice('Analysis complete.')
    } catch (error) {
      const msg = error.response?.data?.message || error.response?.data?.error || 'Unable to analyze resume.'
      setNotice(`Analysis failed: ${msg}`)
    } finally {
      setBusy(false)
    }
  }

  const handleGenerate = async () => {
    if (!selectedResume?.id || !selectedAnalysis?.id) {
      setNotice('Generate an analysis first to build a stronger resume.')
      return
    }

    setBusy(true)
    try {
      const response = await api.post(`/resumes/${selectedResume.id}/generate?analysisId=${selectedAnalysis.id}`, null, getAuthConfig(auth.token))
      setNotice('Improved resume generated. Review it before downloading.')
      setEditingGeneratedId(String(response.data.id))
      setEditingContent(response.data.content || '')
      await loadDashboard()
    } catch (error) {
      setNotice(error.response?.data?.message || 'Unable to generate improved resume.')
    } finally {
      setBusy(false)
    }
  }

  const handleSaveGenerated = async () => {
    if (!editingGeneratedId) return
    setSavingGenerated(true)
    try {
      const response = await api.patch(`/generated-resumes/${editingGeneratedId}`, { content: editingContent }, getAuthConfig(auth.token))
      setGeneratedResumes((current) => current.map((item) => String(item.id) === String(response.data.id) ? response.data : item))
      setNotice('Your updated resume was saved.')
    } catch (error) {
      setNotice(error.response?.data?.message || 'Unable to save the updated resume.')
    } finally {
      setSavingGenerated(false)
    }
  }

  const handleDownload = async (url, filename) => {
    try {
      const response = await api.get(url, {
        responseType: 'blob',
        ...getAuthConfig(auth.token),
      })

      const objectUrl = window.URL.createObjectURL(new Blob([response.data]))
      const anchor = document.createElement('a')
      anchor.href = objectUrl
      anchor.download = filename
      anchor.click()
      window.setTimeout(() => window.URL.revokeObjectURL(objectUrl), 1000)
    } catch (error) {
      setNotice(error.response?.data?.message || 'Download failed.')
    }
  }

  const logout = async () => {
    try {
      await api.post('/auth/logout', null, getAuthConfig(auth.token))
    } catch {
      // Clearing local credentials still signs the browser out if the API is unavailable.
    }
    setAuth(null)
  }

  return (
    <div className="dashboard-shell">
      <aside className="sidebar">
        <div className="brand-wrap">
          <div className="brand-mark">
            <Sparkles size={18} />
          </div>
          <span>ResumeIQ</span>
        </div>

        <div className="sidebar-card user-card">
          <p className="muted-label">Signed in</p>
          <strong>{auth.user?.fullName || 'User'}</strong>
          <span>{auth.user?.email}</span>
        </div>

        <div className="sidebar-card">
          <p className="muted-label">Overview</p>
          <ul className="stat-list">
            <li>
              <span>Resumes</span>
              <strong>{resumes.length}</strong>
            </li>
            <li>
              <span>Analyses</span>
              <strong>{analyses.length}</strong>
            </li>
            <li>
              <span>Variants</span>
              <strong>{generatedResumes.length}</strong>
            </li>
          </ul>
        </div>

        <button className="ghost-btn full-width" type="button" onClick={logout}>
          <LogOut size={16} />
          Log out
        </button>
      </aside>

      <main className="dashboard-content">
        <header className="dashboard-header">
          <div>
            <span className="eyebrow">Career intelligence</span>
            <h1>Resume dashboard</h1>
          </div>
          <label className={`upload-box ${busy ? 'disabled' : ''}`}>
            <Upload size={18} />
            <span>Upload resume</span>
            <input type="file" accept=".pdf,.docx" disabled={busy} onChange={handleUpload} />
          </label>
        </header>

        {notice && <div className="notice-banner">{notice}</div>}

        {loading ? (
          <div className="empty-panel">Loading your resume workspace…</div>
        ) : (
          <>
            <section className="stats-grid">
              <div className="stat-card">
                <span className="muted-label">Current ATS</span>
                <strong>{selectedAnalysis?.atsScore ?? 0}</strong>
                <small>ATS score</small>
              </div>
              <div className="stat-card">
                <span className="muted-label">Overall fit</span>
                <strong>{selectedAnalysis?.overallScore ?? 0}</strong>
                <small>Job match</small>
              </div>
              <div className="stat-card">
                <span className="muted-label">Keywords</span>
                <strong>{(selectedAnalysis?.matchedKeywords || '').split(',').filter(Boolean).length}</strong>
                <small>Matched</small>
              </div>
              <div className="stat-card">
                <span className="muted-label">Variants</span>
                <strong>{generatedResumes.length}</strong>
                <small>Generated</small>
              </div>
            </section>

            <section className="panel-grid">
              <div className="panel">
                <div className="panel-header">
                  <h2>Resumes</h2>
                </div>
                <div className="resume-list">
                  {resumes.length === 0 ? (
                    <div className="empty-panel">No resumes uploaded yet.</div>
                  ) : (
                    resumes.map((resume) => (
                      <button
                        key={resume.id}
                        type="button"
                        className={`resume-item ${selectedResume?.id === resume.id ? 'selected' : ''}`}
                        onClick={() => { setSelectedResumeId(String(resume.id)); setSelectedAnalysisId('') }}
                      >
                        <div>
                          <strong>{resume.originalFilename}</strong>
                          <span>{resume.fileType} • {(resume.fileSize / 1024).toFixed(1)} KB</span>
                        </div>
                        <span className="tag">{resume.processingStatus}</span>
                      </button>
                    ))
                  )}
                </div>
              </div>

              <div className="panel">
                <div className="panel-header">
                  <h2>Target role</h2>
                </div>
                <textarea
                  className="job-description"
                  value={jobDescription}
                  onChange={(event) => setJobDescription(event.target.value)}
                  rows={10}
                  placeholder="Optional: paste a job description for job-specific ATS screening. Leave blank for a general resume review."
                />
                <button type="button" className="primary-btn full-width" onClick={handleAnalyze} disabled={busy || !selectedResume}>
                  {busy ? 'Working…' : jobDescription.trim() ? 'Analyze job fit' : 'Review resume'}
                </button>
              </div>
            </section>

            <section className="panel">
              <div className="panel-header">
                <h2>Insights</h2>
                {selectedAnalysis && (
                  <button type="button" className="ghost-btn" onClick={handleGenerate} disabled={busy || !selectedResume || !selectedAnalysis}>
                    <Wand2 size={16} />
                    {busy ? 'Generating…' : 'Generate improved resume'}
                  </button>
                )}
              </div>

              {selectedAnalysis ? (
                <div className="analysis-layout">
                  <div className="score-panel">
                    <div
                      className="score-ring large"
                      style={{ background: `conic-gradient(#4f46e5 0 ${selectedAnalysis.atsScore}%, #f1f3f8 ${selectedAnalysis.atsScore}% 100%)` }}
                    >
                      <strong>{selectedAnalysis.atsScore}</strong>
                      <span>ATS</span>
                    </div>
                    <div className="score-metric">
                      <span>Overall fit</span>
                      <strong>{selectedAnalysis.overallScore}</strong>
                    </div>
                  </div>

                  <div className="analysis-groups">
                    <div className="analysis-group positive">
                      <h3>
                        <CheckCircle2 size={18} /> Matched skills
                      </h3>
                      <p>{selectedAnalysis.matchedSkills || 'No explicit matched skills recorded yet.'}</p>
                    </div>
                    <div className="analysis-group negative">
                      <h3>
                        <XCircle size={18} /> Missing skills
                      </h3>
                      <p>{selectedAnalysis.missingSkills || 'No critical gaps identified yet.'}</p>
                    </div>
                    <div className="analysis-group neutral">
                      <h3>Recommendations</h3>
                      <p>{selectedAnalysis.recommendations || 'Add stronger impact statements and measurable outcomes.'}</p>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="empty-panel">Run a role analysis to see ATS and fit recommendations.</div>
              )}
            </section>

            {editingGeneratedId && (
              <section className="panel">
                <div className="panel-header">
                  <h2>Review your improved resume</h2>
                  <span className="tag">Editable draft</span>
                </div>
                <p className="muted-label">Review every detail before using this resume. The generator preserves source information and does not verify factual accuracy.</p>
                <textarea
                  className="job-description resume-editor"
                  value={editingContent}
                  onChange={(event) => setEditingContent(event.target.value)}
                  rows={18}
                  aria-label="Edit improved resume content"
                />
                <div className="hero-actions">
                  <button type="button" className="primary-btn" onClick={handleSaveGenerated} disabled={savingGenerated}>
                    {savingGenerated ? 'Saving…' : 'Save changes'}
                  </button>
                  <button type="button" className="secondary-btn" onClick={() => handleDownload(`/generated-resumes/${editingGeneratedId}/download?format=docx`, 'improved-resume.docx')}>
                    <Download size={16} /> Download DOCX
                  </button>
                  <button type="button" className="secondary-btn" onClick={() => handleDownload(`/generated-resumes/${editingGeneratedId}/download?format=pdf`, 'improved-resume.pdf')}>
                    <Download size={16} /> Download PDF
                  </button>
                </div>
              </section>
            )}

            <section className="panel">
              <div className="panel-header">
                <h2>Generated resume variants</h2>
              </div>

              {generatedResumes.length === 0 ? (
                <div className="empty-panel">No improved versions generated yet.</div>
              ) : (
                <div className="generated-list">
                  {generatedResumes.map((variant) => (
                    <div key={variant.id} className="generated-item">
                      <div>
                        <strong>{variant.filename}</strong>
                        <span>{variant.fileType}</span>
                      </div>
                      <div className="generated-actions">
                        <button type="button" className="ghost-btn" onClick={() => { setEditingGeneratedId(String(variant.id)); setEditingContent(variant.content || '') }}>
                          Edit
                        </button>
                        <button type="button" className="ghost-btn" onClick={() => handleDownload(`/generated-resumes/${variant.id}/download?format=docx`, 'improved-resume.docx')}>
                          <Download size={16} /> DOCX
                        </button>
                        <button type="button" className="ghost-btn" onClick={() => handleDownload(`/generated-resumes/${variant.id}/download?format=pdf`, 'improved-resume.pdf')}>
                          <Download size={16} /> PDF
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </>
        )}
      </main>
    </div>
  )
}

export default App
