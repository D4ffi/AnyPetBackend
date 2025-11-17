# Guía de Integración: AnyPet React Native (Expo 54) ↔ Backend API

Esta guía te ayudará a conectar tu aplicación React Native (Expo 54) con el backend de AnyPet para gestionar mascotas, vacunas, recordatorios y almacenar imágenes en AWS S3.

## Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Configuración de Firebase](#configuración-de-firebase)
3. [Instalación de Dependencias](#instalación-de-dependencias)
4. [Configuración de Axios](#configuración-de-axios)
5. [Autenticación](#autenticación)
6. [Endpoints Disponibles](#endpoints-disponibles)
   - [Imágenes (S3)](#1-imágenes-s3)
   - [Recordatorios](#2-recordatorios)
   - [Vacunas](#3-vacunas)
7. [Ejemplos de Código](#ejemplos-de-código)
8. [Manejo de Errores](#manejo-de-errores)
9. [Mejores Prácticas](#mejores-prácticas)

---

## Requisitos Previos

### Backend
- ✅ Backend desplegado y accesible (URL: `https://tu-backend.com` o `http://localhost:8080` para desarrollo)
- ✅ Bucket de AWS S3 configurado (`anypet-images-bucket`)
- ✅ Firebase proyecto configurado con Authentication habilitado
- ✅ Firebase Cloud Messaging (FCM) configurado para notificaciones push

### React Native / Expo
- Node.js 18+ instalado
- Expo CLI instalado: `npm install -g expo-cli`
- Expo SDK 54
- React Native (incluido con Expo)

---

## Configuración de Firebase

### 1. Crear proyecto en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita **Authentication** → Sign-in method → Email/Password
4. Habilita **Cloud Messaging** para notificaciones push

### 2. Configurar Firebase para Expo

Expo 54 usa **Expo Application Services (EAS)** para Firebase. Necesitas obtener las credenciales:

**Para iOS:**
- Descarga el archivo `GoogleService-Info.plist`

**Para Android:**
- Descarga el archivo `google-services.json`

**Para Web:**
- Obtén la configuración web de Firebase

---

## Instalación de Dependencias

```bash
# Navegador de archivos y selección de imágenes
npx expo install expo-image-picker expo-file-system

# Firebase (usando la versión compatible con Expo)
npm install firebase

# HTTP client
npm install axios

# Notificaciones push
npx expo install expo-notifications expo-device expo-constants

# Navegación (si aún no la tienes)
npm install @react-navigation/native @react-navigation/stack
npx expo install react-native-screens react-native-safe-area-context

# AsyncStorage para almacenamiento local
npx expo install @react-native-async-storage/async-storage

# Gestos (para mejor UX)
npx expo install react-native-gesture-handler react-native-reanimated

# Visor de imágenes
npx expo install expo-image
```

---

## Configuración de Firebase

### 1. Crear archivo de configuración

Crea `src/config/firebase.js`:

```javascript
import { initializeApp } from 'firebase/app';
import { getAuth, initializeAuth, getReactNativePersistence } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import AsyncStorage from '@react-native-async-storage/async-storage';

// Tu configuración de Firebase
// Obtén estos valores de Firebase Console → Project Settings → General → Your apps
const firebaseConfig = {
  apiKey: "TU_API_KEY",
  authDomain: "tu-proyecto.firebaseapp.com",
  projectId: "tu-proyecto-id",
  storageBucket: "tu-proyecto.appspot.com",
  messagingSenderId: "123456789",
  appId: "1:123456789:web:abcdef123456",
  // measurementId opcional para Analytics
};

// Inicializar Firebase
const app = initializeApp(firebaseConfig);

// Inicializar Auth con persistencia
const auth = initializeAuth(app, {
  persistence: getReactNativePersistence(AsyncStorage)
});

// Inicializar Firestore (opcional, para sincronización)
const db = getFirestore(app);

export { auth, db };
export default app;
```

### 2. Variables de entorno

Crea un archivo `.env` en la raíz del proyecto:

```env
EXPO_PUBLIC_API_URL=http://localhost:8080
EXPO_PUBLIC_FIREBASE_API_KEY=TU_API_KEY
EXPO_PUBLIC_FIREBASE_AUTH_DOMAIN=tu-proyecto.firebaseapp.com
EXPO_PUBLIC_FIREBASE_PROJECT_ID=tu-proyecto-id
EXPO_PUBLIC_FIREBASE_STORAGE_BUCKET=tu-proyecto.appspot.com
EXPO_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=123456789
EXPO_PUBLIC_FIREBASE_APP_ID=1:123456789:web:abcdef123456
```

Luego usa en `firebase.js`:

```javascript
const firebaseConfig = {
  apiKey: process.env.EXPO_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.EXPO_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.EXPO_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.EXPO_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.EXPO_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.EXPO_PUBLIC_FIREBASE_APP_ID,
};
```

---

## Configuración de Axios

### 1. Crear cliente API

Crea `src/api/client.js`:

```javascript
import axios from 'axios';
import { auth } from '../config/firebase';

const API_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080';

// Crear instancia de axios
const apiClient = axios.create({
  baseURL: API_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar token automáticamente
apiClient.interceptors.request.use(
  async (config) => {
    try {
      const user = auth.currentUser;
      if (user) {
        const token = await user.getIdToken();
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch (error) {
      console.error('Error getting auth token:', error);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejo de errores
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      console.log('Token expired, user needs to login again');
      // Aquí podrías disparar un evento para logout
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

---

## Autenticación

### 1. Servicio de autenticación

Crea `src/services/authService.js`:

```javascript
import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut,
  sendPasswordResetEmail,
  onAuthStateChanged
} from 'firebase/auth';
import { auth } from '../config/firebase';

class AuthService {
  // Login con email y contraseña
  async login(email, password) {
    try {
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const token = await userCredential.user.getIdToken();
      return {
        user: userCredential.user,
        token,
      };
    } catch (error) {
      throw this.handleAuthError(error);
    }
  }

  // Registro de nuevo usuario
  async register(email, password) {
    try {
      const userCredential = await createUserWithEmailAndPassword(auth, email, password);
      const token = await userCredential.user.getIdToken();
      return {
        user: userCredential.user,
        token,
      };
    } catch (error) {
      throw this.handleAuthError(error);
    }
  }

  // Logout
  async logout() {
    try {
      await signOut(auth);
    } catch (error) {
      throw this.handleAuthError(error);
    }
  }

  // Recuperar contraseña
  async resetPassword(email) {
    try {
      await sendPasswordResetEmail(auth, email);
    } catch (error) {
      throw this.handleAuthError(error);
    }
  }

  // Obtener usuario actual
  getCurrentUser() {
    return auth.currentUser;
  }

  // Obtener token actual
  async getCurrentToken() {
    const user = auth.currentUser;
    if (user) {
      return await user.getIdToken();
    }
    return null;
  }

  // Observer de estado de autenticación
  onAuthStateChange(callback) {
    return onAuthStateChanged(auth, callback);
  }

  // Manejo de errores
  handleAuthError(error) {
    const errorMessages = {
      'auth/email-already-in-use': 'Este email ya está registrado',
      'auth/invalid-email': 'Email inválido',
      'auth/weak-password': 'La contraseña debe tener al menos 6 caracteres',
      'auth/user-not-found': 'Usuario no encontrado',
      'auth/wrong-password': 'Contraseña incorrecta',
      'auth/too-many-requests': 'Demasiados intentos, intenta más tarde',
    };

    return new Error(errorMessages[error.code] || error.message);
  }
}

export default new AuthService();
```

### 2. Contexto de autenticación

Crea `src/context/AuthContext.js`:

```javascript
import React, { createContext, useState, useEffect, useContext } from 'react';
import authService from '../services/authService';

const AuthContext = createContext({});

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Escuchar cambios en el estado de autenticación
    const unsubscribe = authService.onAuthStateChange((user) => {
      setUser(user);
      setLoading(false);
    });

    return unsubscribe;
  }, []);

  const login = async (email, password) => {
    const result = await authService.login(email, password);
    setUser(result.user);
    return result;
  };

  const register = async (email, password) => {
    const result = await authService.register(email, password);
    setUser(result.user);
    return result;
  };

  const logout = async () => {
    await authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
```

---

## Endpoints Disponibles

### 1. Imágenes (S3)

#### 1.1 Subir Imagen de Perfil de Mascota

Crea `src/api/imageApi.js`:

```javascript
import apiClient from './client';
import * as FileSystem from 'expo-file-system';

class ImageAPI {
  /**
   * Sube una imagen de perfil de mascota
   * @param {number} petId - ID de la mascota
   * @param {string} imageUri - URI local de la imagen
   * @returns {Promise<Object>} - Respuesta con URLs de imagen y thumbnail
   */
  async uploadPetProfileImage(petId, imageUri) {
    try {
      // Crear FormData
      const formData = new FormData();

      // En React Native/Expo, FormData acepta objetos con uri, type y name
      const filename = imageUri.split('/').pop();
      const match = /\.(\w+)$/.exec(filename);
      const type = match ? `image/${match[1]}` : 'image/jpeg';

      formData.append('file', {
        uri: imageUri,
        name: filename,
        type,
      });

      const response = await apiClient.post(
        `/api/images/pet/${petId}/profile`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Sube una imagen de lote de vacuna
   * @param {number} vaccinationRecordId - ID del registro de vacunación
   * @param {string} imageUri - URI local de la imagen
   * @returns {Promise<Object>} - Respuesta con URLs de imagen y thumbnail
   */
  async uploadVaccineBatchImage(vaccinationRecordId, imageUri) {
    try {
      const formData = new FormData();

      const filename = imageUri.split('/').pop();
      const match = /\.(\w+)$/.exec(filename);
      const type = match ? `image/${match[1]}` : 'image/jpeg';

      formData.append('file', {
        uri: imageUri,
        name: filename,
        type,
      });

      const response = await apiClient.post(
        `/api/images/vaccine/${vaccinationRecordId}/batch`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Elimina una imagen
   * @param {string} imageUrl - URL de la imagen en S3
   * @returns {Promise<Object>} - Confirmación de eliminación
   */
  async deleteImage(imageUrl) {
    try {
      const response = await apiClient.delete('/api/images', {
        params: { url: imageUrl },
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Verifica si una imagen existe
   * @param {string} imageUrl - URL de la imagen en S3
   * @returns {Promise<boolean>} - true si existe
   */
  async checkImageExists(imageUrl) {
    try {
      const response = await apiClient.get('/api/images/exists', {
        params: { url: imageUrl },
      });
      return response.data.exists;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Obtiene metadata de una imagen
   * @param {string} imageUrl - URL de la imagen en S3
   * @returns {Promise<Object>} - Metadata de la imagen
   */
  async getImageMetadata(imageUrl) {
    try {
      const response = await apiClient.get('/api/images/metadata', {
        params: { url: imageUrl },
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  handleError(error) {
    if (error.response) {
      return new Error(error.response.data.message || 'Error al procesar imagen');
    }
    return error;
  }
}

export default new ImageAPI();
```

#### Uso con Expo ImagePicker:

```javascript
import * as ImagePicker from 'expo-image-picker';
import imageApi from '../api/imageApi';

// Componente de ejemplo
const PetProfileScreen = ({ petId }) => {
  const [uploading, setUploading] = useState(false);
  const [imageUrl, setImageUrl] = useState(null);

  const pickAndUploadImage = async () => {
    // Solicitar permisos
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') {
      alert('Se necesitan permisos para acceder a la galería');
      return;
    }

    // Abrir selector de imágenes
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8, // Comprimir antes de subir
    });

    if (!result.canceled && result.assets[0]) {
      setUploading(true);
      try {
        const response = await imageApi.uploadPetProfileImage(
          petId,
          result.assets[0].uri
        );

        if (response.success) {
          setImageUrl(response.imageUrl);
          alert('Imagen subida exitosamente');
        }
      } catch (error) {
        alert('Error al subir imagen: ' + error.message);
      } finally {
        setUploading(false);
      }
    }
  };

  return (
    <View>
      <TouchableOpacity onPress={pickAndUploadImage} disabled={uploading}>
        <Text>{uploading ? 'Subiendo...' : 'Seleccionar imagen'}</Text>
      </TouchableOpacity>

      {imageUrl && (
        <Image
          source={{ uri: imageUrl }}
          style={{ width: 200, height: 200 }}
        />
      )}
    </View>
  );
};
```

---

### 2. Recordatorios

Crea `src/api/reminderApi.js`:

```javascript
import apiClient from './client';

class ReminderAPI {
  /**
   * Crea un nuevo recordatorio
   * @param {Object} reminderData - Datos del recordatorio
   * @returns {Promise<Object>} - Recordatorio creado
   */
  async createReminder(reminderData) {
    try {
      const response = await apiClient.post('/api/reminders', reminderData);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Obtiene todos los recordatorios de un usuario
   * @param {string} userId - Firebase UID
   * @returns {Promise<Array>} - Lista de recordatorios
   */
  async getRemindersByUserId(userId) {
    try {
      const response = await apiClient.get(`/api/reminders/user/${userId}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Obtiene solo recordatorios activos de un usuario
   * @param {string} userId - Firebase UID
   * @returns {Promise<Array>} - Lista de recordatorios activos
   */
  async getActiveReminders(userId) {
    try {
      const response = await apiClient.get(`/api/reminders/user/${userId}/active`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Obtiene un recordatorio por ID
   * @param {number} reminderId - ID del recordatorio
   * @returns {Promise<Object>} - Recordatorio
   */
  async getReminderById(reminderId) {
    try {
      const response = await apiClient.get(`/api/reminders/${reminderId}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Actualiza un recordatorio
   * @param {number} reminderId - ID del recordatorio
   * @param {Object} reminderData - Datos actualizados
   * @returns {Promise<Object>} - Recordatorio actualizado
   */
  async updateReminder(reminderId, reminderData) {
    try {
      const response = await apiClient.put(`/api/reminders/${reminderId}`, reminderData);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Elimina un recordatorio
   * @param {number} reminderId - ID del recordatorio
   * @returns {Promise<Object>} - Confirmación
   */
  async deleteReminder(reminderId) {
    try {
      const response = await apiClient.delete(`/api/reminders/${reminderId}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Sincroniza recordatorios desde Firestore
   * @param {string} userId - Firebase UID
   * @returns {Promise<Object>} - Confirmación
   */
  async syncFromFirestore(userId) {
    try {
      const response = await apiClient.post(`/api/reminders/sync/${userId}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  handleError(error) {
    if (error.response) {
      return new Error(error.response.data.message || 'Error en recordatorios');
    }
    return error;
  }
}

export default new ReminderAPI();
```

#### Configurar Notificaciones Push

Crea `src/services/notificationService.js`:

```javascript
import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import { Platform } from 'react-native';
import Constants from 'expo-constants';

// Configurar comportamiento de notificaciones
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
  }),
});

class NotificationService {
  /**
   * Registra el dispositivo para notificaciones push
   * @returns {Promise<string>} - Token de notificaciones (FCM/APNs)
   */
  async registerForPushNotifications() {
    let token;

    if (Device.isDevice) {
      const { status: existingStatus } = await Notifications.getPermissionsAsync();
      let finalStatus = existingStatus;

      if (existingStatus !== 'granted') {
        const { status } = await Notifications.requestPermissionsAsync();
        finalStatus = status;
      }

      if (finalStatus !== 'granted') {
        throw new Error('No se otorgaron permisos para notificaciones');
      }

      // Obtener token
      token = await Notifications.getExpoPushTokenAsync({
        projectId: Constants.expoConfig?.extra?.eas?.projectId,
      });

      console.log('Push token:', token.data);
    } else {
      console.log('Debe usar un dispositivo físico para notificaciones push');
    }

    // Configuración específica de Android
    if (Platform.OS === 'android') {
      Notifications.setNotificationChannelAsync('default', {
        name: 'default',
        importance: Notifications.AndroidImportance.MAX,
        vibrationPattern: [0, 250, 250, 250],
        lightColor: '#FF231F7C',
      });
    }

    return token?.data;
  }

  /**
   * Programa una notificación local
   * @param {Object} notification - Datos de la notificación
   * @param {Date} trigger - Fecha/hora de la notificación
   */
  async scheduleNotification(notification, trigger) {
    await Notifications.scheduleNotificationAsync({
      content: {
        title: notification.title,
        body: notification.message,
        data: notification.data || {},
      },
      trigger,
    });
  }

  /**
   * Cancela todas las notificaciones programadas
   */
  async cancelAllNotifications() {
    await Notifications.cancelAllScheduledNotificationsAsync();
  }

  /**
   * Listener para notificaciones recibidas
   */
  addNotificationReceivedListener(callback) {
    return Notifications.addNotificationReceivedListener(callback);
  }

  /**
   * Listener para cuando se toca una notificación
   */
  addNotificationResponseReceivedListener(callback) {
    return Notifications.addNotificationResponseReceivedListener(callback);
  }
}

export default new NotificationService();
```

#### Ejemplo de uso de recordatorios:

```javascript
import React, { useState, useEffect } from 'react';
import { View, Text, Button, FlatList } from 'react-native';
import reminderApi from '../api/reminderApi';
import notificationService from '../services/notificationService';
import { useAuth } from '../context/AuthContext';

const RemindersScreen = () => {
  const { user } = useAuth();
  const [reminders, setReminders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadReminders();
    setupNotifications();
  }, []);

  const setupNotifications = async () => {
    try {
      const token = await notificationService.registerForPushNotifications();
      console.log('Notification token:', token);
    } catch (error) {
      console.error('Error setting up notifications:', error);
    }
  };

  const loadReminders = async () => {
    try {
      const response = await reminderApi.getActiveReminders(user.uid);
      setReminders(response.reminders || []);
    } catch (error) {
      console.error('Error loading reminders:', error);
    } finally {
      setLoading(false);
    }
  };

  const createReminder = async () => {
    try {
      const token = await notificationService.registerForPushNotifications();

      const reminderData = {
        title: 'Alimentar a mi mascota',
        message: 'Es hora de alimentar a tu mascota',
        scheduledTime: '09:00',
        repeatInterval: 'DAILY',
        userId: user.uid,
        petId: null,
        deviceToken: token,
        active: true,
      };

      const response = await reminderApi.createReminder(reminderData);

      if (response.success) {
        alert('Recordatorio creado');
        loadReminders();
      }
    } catch (error) {
      alert('Error al crear recordatorio: ' + error.message);
    }
  };

  const deleteReminder = async (reminderId) => {
    try {
      await reminderApi.deleteReminder(reminderId);
      alert('Recordatorio eliminado');
      loadReminders();
    } catch (error) {
      alert('Error al eliminar: ' + error.message);
    }
  };

  return (
    <View style={{ flex: 1, padding: 20 }}>
      <Button title="Crear Recordatorio" onPress={createReminder} />

      <FlatList
        data={reminders}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => (
          <View style={{ padding: 10, borderBottomWidth: 1 }}>
            <Text style={{ fontSize: 16, fontWeight: 'bold' }}>
              {item.title}
            </Text>
            <Text>{item.message}</Text>
            <Text>Hora: {item.scheduledTime}</Text>
            <Text>Repetir: {item.repeatInterval}</Text>
            <Button
              title="Eliminar"
              onPress={() => deleteReminder(item.id)}
              color="red"
            />
          </View>
        )}
      />
    </View>
  );
};

export default RemindersScreen;
```

**Intervalos disponibles:**
- `ONCE` - Una vez
- `DAILY` - Diario
- `WEEKLY` - Semanal
- `MONTHLY` - Mensual
- `YEARLY` - Anual
- `EVERY_HOUR` - Cada hora
- `EVERY_2_HOURS` - Cada 2 horas
- `EVERY_4_HOURS` - Cada 4 horas
- `EVERY_6_HOURS` - Cada 6 horas
- `EVERY_12_HOURS` - Cada 12 horas

---

### 3. Vacunas

Crea `src/api/vaccineApi.js`:

```javascript
import apiClient from './client';

class VaccineAPI {
  /**
   * Obtiene todas las vacunas
   * @returns {Promise<Array>} - Lista de vacunas
   */
  async getAllVaccines() {
    try {
      const response = await apiClient.get('/api/vaccines');
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Obtiene vacunas por tipo de mascota
   * @param {string} petType - Tipo de mascota (CAT, DOG, BIRD, etc.)
   * @returns {Promise<Array>} - Lista de vacunas
   */
  async getVaccinesByPetType(petType) {
    try {
      const response = await apiClient.get(`/api/vaccines/pet-type/${petType.toUpperCase()}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Obtiene solo vacunas esenciales (core) por tipo de mascota
   * @param {string} petType - Tipo de mascota
   * @returns {Promise<Array>} - Lista de vacunas esenciales
   */
  async getCoreVaccines(petType) {
    try {
      const response = await apiClient.get(`/api/vaccines/pet-type/${petType.toUpperCase()}/core`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Obtiene estadísticas de vacunas por tipo de mascota
   * @param {string} petType - Tipo de mascota
   * @returns {Promise<Object>} - Estadísticas
   */
  async getVaccineStatistics(petType) {
    try {
      const response = await apiClient.get(`/api/vaccines/statistics/${petType.toUpperCase()}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Obtiene una vacuna por ID
   * @param {number} vaccineId - ID de la vacuna
   * @returns {Promise<Object>} - Vacuna
   */
  async getVaccineById(vaccineId) {
    try {
      const response = await apiClient.get(`/api/vaccines/${vaccineId}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Crea una nueva vacuna (Admin)
   * @param {Object} vaccineData - Datos de la vacuna
   * @returns {Promise<Object>} - Vacuna creada
   */
  async createVaccine(vaccineData) {
    try {
      const response = await apiClient.post('/api/vaccines', vaccineData);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Actualiza una vacuna (Admin)
   * @param {number} vaccineId - ID de la vacuna
   * @param {Object} vaccineData - Datos actualizados
   * @returns {Promise<Object>} - Vacuna actualizada
   */
  async updateVaccine(vaccineId, vaccineData) {
    try {
      const response = await apiClient.put(`/api/vaccines/${vaccineId}`, vaccineData);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Elimina una vacuna (Admin)
   * @param {number} vaccineId - ID de la vacuna
   * @returns {Promise<void>}
   */
  async deleteVaccine(vaccineId) {
    try {
      await apiClient.delete(`/api/vaccines/${vaccineId}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  handleError(error) {
    if (error.response) {
      return new Error(error.response.data.message || 'Error en vacunas');
    }
    return error;
  }
}

export default new VaccineAPI();
```

#### Ejemplo de uso:

```javascript
import React, { useState, useEffect } from 'react';
import { View, Text, FlatList, ActivityIndicator } from 'react-native';
import vaccineApi from '../api/vaccineApi';

const VaccineListScreen = ({ petType }) => {
  const [vaccines, setVaccines] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadVaccines();
  }, [petType]);

  const loadVaccines = async () => {
    try {
      const data = await vaccineApi.getVaccinesByPetType(petType);
      setVaccines(data);
    } catch (error) {
      alert('Error al cargar vacunas: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <ActivityIndicator size="large" />;
  }

  return (
    <FlatList
      data={vaccines}
      keyExtractor={(item) => item.id.toString()}
      renderItem={({ item }) => (
        <View style={{ padding: 15, borderBottomWidth: 1 }}>
          <Text style={{ fontSize: 18, fontWeight: 'bold' }}>
            {item.name}
          </Text>
          <Text>{item.description}</Text>
          <Text>
            {item.isCore ? '✓ Vacuna esencial' : 'Vacuna opcional'}
          </Text>
          {item.recommendedAgeWeeks && (
            <Text>Edad recomendada: {item.recommendedAgeWeeks} semanas</Text>
          )}
        </View>
      )}
    />
  );
};

export default VaccineListScreen;
```

---

## Manejo de Errores

### Hook personalizado para manejo de errores

```javascript
// src/hooks/useApi.js
import { useState } from 'react';

export const useApi = (apiFunc) => {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const request = async (...args) => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiFunc(...args);
      setData(result);
      return result;
    } catch (err) {
      setError(err.message || 'Error desconocido');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { data, error, loading, request };
};

// Uso
const MyComponent = () => {
  const { data, error, loading, request } = useApi(vaccineApi.getAllVaccines);

  useEffect(() => {
    request();
  }, []);

  if (loading) return <ActivityIndicator />;
  if (error) return <Text>Error: {error}</Text>;
  if (!data) return null;

  return <Text>Data loaded!</Text>;
};
```

---

## Mejores Prácticas

### 1. Gestión de Estado

Usa Context API o Redux para compartir datos entre componentes:

```javascript
// src/context/PetContext.js
import React, { createContext, useState, useContext } from 'react';

const PetContext = createContext({});

export const PetProvider = ({ children }) => {
  const [pets, setPets] = useState([]);
  const [selectedPet, setSelectedPet] = useState(null);

  return (
    <PetContext.Provider value={{ pets, setPets, selectedPet, setSelectedPet }}>
      {children}
    </PetContext.Provider>
  );
};

export const usePets = () => useContext(PetContext);
```

### 2. Cache de Imágenes

Usa `expo-image` para mejor rendimiento:

```javascript
import { Image } from 'expo-image';

const PetAvatar = ({ imageUrl }) => (
  <Image
    source={{ uri: imageUrl }}
    style={{ width: 100, height: 100, borderRadius: 50 }}
    contentFit="cover"
    transition={300}
    cachePolicy="memory-disk" // Cache en memoria y disco
  />
);
```

### 3. Manejo de Conectividad

```javascript
import NetInfo from '@react-native-community/netinfo';

// Verificar conectividad antes de hacer requests
const checkConnectivity = async () => {
  const state = await NetInfo.fetch();
  return state.isConnected;
};

// Uso
const uploadImage = async () => {
  const isConnected = await checkConnectivity();

  if (!isConnected) {
    alert('No hay conexión a internet');
    return;
  }

  // Proceder con el upload
};
```

### 4. Compresión de Imágenes

```javascript
import { manipulateAsync, SaveFormat } from 'expo-image-manipulator';

const compressImage = async (uri) => {
  const manipResult = await manipulateAsync(
    uri,
    [{ resize: { width: 1000 } }], // Redimensionar
    { compress: 0.7, format: SaveFormat.JPEG } // Comprimir
  );
  return manipResult.uri;
};

// Uso al seleccionar imagen
const pickImage = async () => {
  const result = await ImagePicker.launchImageLibraryAsync({...});

  if (!result.canceled) {
    const compressedUri = await compressImage(result.assets[0].uri);
    await imageApi.uploadPetProfileImage(petId, compressedUri);
  }
};
```

---

## Estructura del Proyecto Recomendada

```
src/
├── api/
│   ├── client.js              # Configuración de axios
│   ├── imageApi.js            # Endpoints de imágenes
│   ├── reminderApi.js         # Endpoints de recordatorios
│   └── vaccineApi.js          # Endpoints de vacunas
├── components/
│   ├── PetCard.js
│   ├── VaccineItem.js
│   └── ReminderItem.js
├── config/
│   └── firebase.js            # Configuración de Firebase
├── context/
│   ├── AuthContext.js         # Contexto de autenticación
│   └── PetContext.js          # Contexto de mascotas
├── hooks/
│   └── useApi.js              # Hook personalizado para API calls
├── screens/
│   ├── LoginScreen.js
│   ├── PetListScreen.js
│   ├── PetDetailScreen.js
│   ├── RemindersScreen.js
│   └── VaccinesScreen.js
├── services/
│   ├── authService.js         # Servicio de autenticación
│   └── notificationService.js # Servicio de notificaciones
└── utils/
    ├── constants.js
    └── helpers.js
```

---

## App.js Completo

```javascript
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import { AuthProvider } from './src/context/AuthContext';
import { PetProvider } from './src/context/PetContext';
import LoginScreen from './src/screens/LoginScreen';
import HomeScreen from './src/screens/HomeScreen';

const Stack = createStackNavigator();

export default function App() {
  return (
    <AuthProvider>
      <PetProvider>
        <NavigationContainer>
          <Stack.Navigator initialRouteName="Login">
            <Stack.Screen
              name="Login"
              component={LoginScreen}
              options={{ headerShown: false }}
            />
            <Stack.Screen
              name="Home"
              component={HomeScreen}
              options={{ title: 'Mis Mascotas' }}
            />
          </Stack.Navigator>
        </NavigationContainer>
      </PetProvider>
    </AuthProvider>
  );
}
```

---

## Configuración de app.json

```json
{
  "expo": {
    "name": "AnyPet",
    "slug": "anypet",
    "version": "1.0.0",
    "orientation": "portrait",
    "icon": "./assets/icon.png",
    "userInterfaceStyle": "light",
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#ffffff"
    },
    "ios": {
      "supportsTablet": true,
      "bundleIdentifier": "com.tuempresa.anypet",
      "googleServicesFile": "./GoogleService-Info.plist"
    },
    "android": {
      "adaptiveIcon": {
        "foregroundImage": "./assets/adaptive-icon.png",
        "backgroundColor": "#ffffff"
      },
      "package": "com.tuempresa.anypet",
      "googleServicesFile": "./google-services.json",
      "permissions": [
        "CAMERA",
        "READ_EXTERNAL_STORAGE",
        "WRITE_EXTERNAL_STORAGE",
        "NOTIFICATIONS"
      ]
    },
    "plugins": [
      [
        "expo-notifications",
        {
          "icon": "./assets/notification-icon.png",
          "color": "#ffffff"
        }
      ]
    ]
  }
}
```

---

## Checklist de Integración

- [ ] Firebase configurado en el proyecto
- [ ] Variables de entorno creadas en `.env`
- [ ] Dependencias instaladas (`expo-image-picker`, `axios`, `firebase`, etc.)
- [ ] `apiClient.js` configurado con interceptores
- [ ] AuthContext implementado
- [ ] Permisos de cámara y galería solicitados
- [ ] Notificaciones push configuradas
- [ ] Probado login/registro con Firebase
- [ ] Probado subir imagen de mascota
- [ ] Probado crear recordatorio
- [ ] Probado obtener lista de vacunas

---

## Comandos Útiles

```bash
# Iniciar en desarrollo
npx expo start

# Iniciar en iOS
npx expo start --ios

# Iniciar en Android
npx expo start --android

# Limpiar cache
npx expo start -c

# Build para producción
eas build --platform ios
eas build --platform android
```

---

¡Listo! Con esta guía tienes todo lo necesario para integrar tu app de React Native (Expo 54) con el backend de AnyPet. 🚀
