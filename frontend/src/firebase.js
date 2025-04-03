import { initializeApp } from "firebase/app";
import { getStorage, ref, uploadBytes, getDownloadURL } from "firebase/storage";

const firebaseConfig = {
    apiKey: "AIzaSyDKBo2srK32dDj4UvxqqKIOjv7uBWhXizY",
    authDomain: "tesis-5b568.firebaseapp.com",
    projectId: "tesis-5b568",
    storageBucket: "tesis-5b568.firebasestorage.app",
    messagingSenderId: "1091443176286",
    appId: "1:1091443176286:web:992600749baa78852a15cf",
    measurementId: "G-5QFZ0TJR1T"
  };

const app = initializeApp(firebaseConfig);
const storage = getStorage(app);

// Función para subir una imagen al storage y obtener su URL
const subirImagen = async (archivo, carpeta = "centros-medicos") => {
  if (!archivo) return null;
  
  try {
    // Crear un nombre único para el archivo
    const nombreArchivo = `${carpeta}/${Date.now()}-${archivo.name}`;
    const storageRef = ref(storage, nombreArchivo);
    
    // Subir el archivo
    await uploadBytes(storageRef, archivo);
    
    // Obtener la URL de descarga
    const url = await getDownloadURL(storageRef);
    return url;
  } catch (error) {
    console.error("Error al subir la imagen:", error);
    return null;
  }
};

export { storage, subirImagen }; 