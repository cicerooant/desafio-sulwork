import { Navigate, Route, Routes } from 'react-router-dom'
import EventosPage from './features/eventos/EventosPage'

export default function App() {
  return (
    <Routes>
      <Route path="/eventos" element={<EventosPage />} />
      <Route path="*" element={<Navigate to="/eventos" replace />} />
    </Routes>
  )
}
