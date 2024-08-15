package io.github.jilipop.adohri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import androidx.core.content.ContextCompat;
import io.github.jilipop.adohri.audio.HeadphoneDisconnectionCallback;
import io.github.jilipop.adohri.audio.HeadphoneDisconnectionHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HeadphoneDisconnectionHandlerTests {
    @Mock
    private Context context;
    @Mock
    private IntentFilter filter;
    @Mock
    private HeadphoneDisconnectionCallback callback;
    @Mock
    private Intent intent;

    private MockedStatic<ContextCompat> contextCompatStaticMock;

    private HeadphoneDisconnectionHandler handler;
    private BroadcastReceiver receiver;

    @Before
    public void setUp() {
        contextCompatStaticMock = mockStatic(ContextCompat.class);
        contextCompatStaticMock.when(() -> ContextCompat.registerReceiver(any(Context.class), any(BroadcastReceiver.class), any(IntentFilter.class), anyInt())).thenAnswer(invocation -> null);

        handler = new HeadphoneDisconnectionHandler(context, filter);
        handler.setHeadphoneDisconnectionCallback(callback);

        ArgumentCaptor<BroadcastReceiver> receiverCaptor = ArgumentCaptor.forClass(BroadcastReceiver.class);
        contextCompatStaticMock.verify(() -> ContextCompat.registerReceiver(eq(context), receiverCaptor.capture(), eq(filter), eq(ContextCompat.RECEIVER_NOT_EXPORTED)));
        receiver = receiverCaptor.getValue();
    }

    @After
    public void tearDown() {
        contextCompatStaticMock.close();
    }

    @Test
    public void onReceive_audioBecomingNoisy_callsOnHeadphonesDisconnected() {
        when(intent.getAction()).thenReturn(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        receiver.onReceive(context, intent);

        verify(callback).onHeadphonesDisconnected();
    }

    @Test
    public void onReceive_headsetPluggedOut_callsOnHeadphonesDisconnected() {
        when(intent.getAction()).thenReturn(Intent.ACTION_HEADSET_PLUG);
        when(intent.getIntExtra("state", -1)).thenReturn(0);

        receiver.onReceive(context, intent);

        verify(callback).onHeadphonesDisconnected();
    }

    @Test
    public void onReceive_headsetPluggedIn_doesNotCallOnHeadphonesDisconnected() {
        when(intent.getAction()).thenReturn(Intent.ACTION_HEADSET_PLUG);
        when(intent.getIntExtra("state", -1)).thenReturn(1);

        receiver.onReceive(context, intent);

        verify(callback, never()).onHeadphonesDisconnected();
    }

    @Test
    public void cleanup_unregistersReceiver() {
        handler.cleanup();

        verify(context).unregisterReceiver(receiver);
    }
}
