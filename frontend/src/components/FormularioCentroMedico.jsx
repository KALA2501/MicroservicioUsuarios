import React, { useState } from 'react';
import { subirImagen } from '../firebase';
import './FormularioCentroMedico.css';

const FormularioCentroMedico = () => {
  const [formData, setFormData] = useState({
    nombre: '',
    telefono: '',
    direccion: '',
    correo: '',
    URLogo: ''
  });
  const [imagenCentro, setImagenCentro] = useState(null);
  const [previewImagen, setPreviewImagen] = useState('');
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleImagenChange = (e) => {
    const archivo = e.target.files[0];
    if (archivo) {
      setImagenCentro(archivo);
      
      // Crear URL para previsualización
      const url = URL.createObjectURL(archivo);
      setPreviewImagen(url);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setCargando(true);
    setMensaje('');

    try {
      // Subir imagen a Firebase Storage si existe
      let urlImagen = '';
      if (imagenCentro) {
        urlImagen = await subirImagen(imagenCentro);
        if (!urlImagen) {
          throw new Error('Error al subir la imagen');
        }
      }

      // Preparar datos para enviar al backend
      const datosParaEnviar = {
        ...formData,
        URLogo: urlImagen
      };

      // Enviar datos al backend
      const response = await fetch('http://localhost:8080/api/centro-medico', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(datosParaEnviar),
      });

      if (!response.ok) {
        throw new Error('Error al guardar el centro médico');
      }

      const data = await response.json();
      setMensaje('Centro médico guardado correctamente');
      
      // Limpiar formulario
      setFormData({
        nombre: '',
        telefono: '',
        direccion: '',
        correo: '',
        URLogo: ''
      });
      setImagenCentro(null);
      setPreviewImagen('');
      
    } catch (error) {
      console.error('Error:', error);
      setMensaje(`Error: ${error.message}`);
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="formulario-centro-medico">
      <h2>Registrar Centro Médico</h2>
      
      {mensaje && (
        <div className={`mensaje ${mensaje.includes('Error') ? 'error' : 'exito'}`}>
          {mensaje}
        </div>
      )}
      
      <form onSubmit={handleSubmit}>
        <div className="campo-formulario">
          <label htmlFor="nombre">Nombre del Centro Médico:</label>
          <input
            type="text"
            id="nombre"
            name="nombre"
            value={formData.nombre}
            onChange={handleChange}
            required
          />
        </div>
        
        <div className="campo-formulario">
          <label htmlFor="telefono">Teléfono:</label>
          <input
            type="tel"
            id="telefono"
            name="telefono"
            value={formData.telefono}
            onChange={handleChange}
            required
          />
        </div>
        
        <div className="campo-formulario">
          <label htmlFor="direccion">Dirección:</label>
          <input
            type="text"
            id="direccion"
            name="direccion"
            value={formData.direccion}
            onChange={handleChange}
            required
          />
        </div>
        
        <div className="campo-formulario">
          <label htmlFor="correo">Correo Electrónico:</label>
          <input
            type="email"
            id="correo"
            name="correo"
            value={formData.correo}
            onChange={handleChange}
            required
          />
        </div>
        
        <div className="campo-formulario">
          <label htmlFor="imagen">Logo del Centro Médico:</label>
          <input
            type="file"
            id="imagen"
            accept="image/*"
            onChange={handleImagenChange}
          />
          
          {previewImagen && (
            <div className="preview-imagen">
              <img src={previewImagen} alt="Vista previa" />
            </div>
          )}
        </div>
        
        <button type="submit" disabled={cargando}>
          {cargando ? 'Guardando...' : 'Guardar Centro Médico'}
        </button>
      </form>
    </div>
  );
};

export default FormularioCentroMedico; 