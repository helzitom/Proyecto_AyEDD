# Proyecto_AyEDD
Proyecto de Algoritmo y Estructura de Datos
# App de Gestión de Pedidos 🚚

## Descripción
App Android para gestión y entrega de pedidos en tiempo real con seguimiento GPS.

## Tecnologías
- Android Studio
- Java
- Firebase Firestore
- Google Maps API
- Firebase Authentication

## Configuración del Proyecto

### 1. Clonar el repositorio
\`\`\`bash
git clone https://github.com/tu-usuario/pedidos-app.git
cd pedidos-app
\`\`\`

### 2. Configurar Firebase
1. Crear proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Descargar \`google-services.json\`
3. Colocar el archivo en \`app/google-services.json\`

### 3. Configurar Google Maps API
1. Obtener API key en [Google Cloud Console](https://console.cloud.google.com/)
2. Crear archivo \`secrets.properties\` en la raíz:
   \`\`\`
   GOOGLE_MAPS_API_KEY=XXXXXXXXX
   \`\`\`

### 4. Compilar
\`\`\`bash
./gradlew assembleDebug
\`\`\`

## Estructura del Proyecto
- \`activities/\` - Activities principales
- \`models/\` - Modelos de datos
- \`services/\` - Servicios de Firebase y ubicación
- \`utils/\` - Utilidades y helpers

## Contribuir
1. Fork el proyecto
2. Crea una rama feature
3. Commit tus cambios
4. Push a la rama
5. Abre un Pull Request
