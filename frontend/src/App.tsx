import { Navigate, Route, Routes } from 'react-router-dom'
import EventosPage from './features/eventos/EventosPage'
import ParticipantesPage from './features/participantes/ParticipantesPage'

export default function App() {
  return (
    <Routes>
      <Route path="/eventos" element={<EventosPage />} />
      <Route path="/participantes" element={<ParticipantesPage />} />
      <Route path="*" element={<Navigate to="/eventos" replace />} />
    </Routes>
  )
}
