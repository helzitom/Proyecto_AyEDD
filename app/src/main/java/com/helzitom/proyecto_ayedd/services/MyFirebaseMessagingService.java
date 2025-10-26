package com.helzitom.proyecto_ayedd.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.helzitom.proyecto_ayedd.R;
import com.helzitom.proyecto_ayedd.activities.TrackOrderActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "pedidos_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "📩 Mensaje recibido de: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            String titulo = remoteMessage.getData().get("titulo");
            String mensaje = remoteMessage.getData().get("mensaje");
            String pedidoId = remoteMessage.getData().get("pedidoId");

            mostrarNotificacion(titulo, mensaje, pedidoId);
        }
    }

    private void mostrarNotificacion(String titulo, String mensaje, String pedidoId) {
        createNotificationChannel();

        Intent intent = new Intent(this, TrackOrderActivity.class);
        intent.putExtra(TrackOrderActivity.EXTRA_PEDIDO_ID, pedidoId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pedidos";
            String description = "Notificaciones de pedidos";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔑 Nuevo token FCM: " + token);
        // Guardar token en Firestore para el usuario
    }
}