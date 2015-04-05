package com.vdxp.gdx.lib;

import com.badlogic.gdx.audio.Music;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.vdxp.gdx.lib.testutil.Matchers.isCloseTo;
import static com.vdxp.gdx.lib.testutil.Matchers.isGreaterThan;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MusicSystemTest {

	public static final float shortTime = (1f / 8f) * MusicSystem.defaultCrossFadeSeconds;
	public static final float halfCrossFade = (4f / 8f) * MusicSystem.defaultCrossFadeSeconds;
	public static final float underCrossFade = (7f / 8f) * MusicSystem.defaultCrossFadeSeconds;
	public static final float longTime = (40f / 8f) * MusicSystem.defaultCrossFadeSeconds;

	@Spy
	public MusicSystem system;

	@Mock
	public Music music1;
	public boolean music1play = false;
	public ArgumentCaptor<Float> volume1 = ArgumentCaptor.forClass(Float.class);

	@Mock
	public Music music2;
	public ArgumentCaptor<Float> volume2 = ArgumentCaptor.forClass(Float.class);

	@Mock
	public Music music3;
	public ArgumentCaptor<Float> volume3 = ArgumentCaptor.forClass(Float.class);

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void playPlaysMusic() {
		system.play(music1);
		system.update(0);
		verify(music1).play();
	}

	@Test
	public void queuePlaysMusicWhenNothingPlaying() {
		system.queue(music1);
		system.update(0);
		verify(music1).play();
	}

	@Test
	public void playThenQueuePlaysFirstMusic() {
		system.play(music1);
		system.update(0);
		verify(music1, times(1)).play();

		when(music1.isPlaying()).thenReturn(true);
		system.queue(music2);
		system.update(0);
		verify(music1, times(1)).play();
		verify(music2, times(0)).play();
	}

	@Test
	public void queuedMusicPlaysWhenCurrentMusicStops() {
		system.play(music1);
		system.update(0);
		verify(music1, times(1)).play();

		when(music1.isPlaying()).thenReturn(false);
		system.queue(music2);
		system.update(0);
		verify(music1, times(1)).play();
		verify(music2, times(1)).play();
	}

	@Test
	public void playClearsQueue() {
		system.queue(music1);
		system.update(0);
		verify(music1, times(1)).play();

		when(music1.isPlaying()).thenReturn(true);
		system.queue(music2);
		system.update(0);
		verify(music1, times(1)).play();
		verify(music2, times(0)).play();

		when(music1.isPlaying()).thenReturn(true);
		when(music2.isPlaying()).thenReturn(false);
		system.play(music3);
		system.update(0);

		verify(music1, times(1)).play();
		verify(music2, times(0)).play();
		verify(music3, times(1)).play();
		assertThat(system.getQueueSize(), is(0));
	}

	@Test
	public void playSetsVolumeHighWhenNothingPlaying() {
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				music1play = true;
				return null;
			}
		}).when(music1).play();
		doAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(final InvocationOnMock invocation) throws Throwable {
				return music1play;
			}
		}).when(music1).isPlaying();

		system.play(music1);
		system.update(0);
		verify(music1, atLeastOnce()).setVolume(volume1.capture());
		assertThat(volume1.getValue(), isCloseTo(1));
	}

	@Test
	public void queueSetsVolumeHighWhenNothingPlaying() {
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				music1play = true;
				return null;
			}
		}).when(music1).play();
		doAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(final InvocationOnMock invocation) throws Throwable {
				return music1play;
			}
		}).when(music1).isPlaying();

		system.queue(music1);
		system.update(0);
		verify(music1, atLeastOnce()).setVolume(volume1.capture());
		assertThat(volume1.getValue(), isCloseTo(1));
	}

	@Test
	public void playCrossFadesNewMusic() {
		system.play(music1);
		system.update(longTime);

		when(music1.isPlaying()).thenReturn(true);
		system.play(music2);
		system.update(0);
		verify(music1, atLeastOnce()).setVolume(volume1.capture());
		verify(music2, atLeastOnce()).setVolume(volume2.capture());
		assertThat(volume1.getValue(), isCloseTo(1));
		assertThat(volume2.getValue(), isCloseTo(0));

		when(music1.isPlaying()).thenReturn(true);
		when(music2.isPlaying()).thenReturn(true);
		system.update(halfCrossFade);
		verify(music1, atLeastOnce()).setVolume(volume1.capture());
		verify(music2, atLeastOnce()).setVolume(volume2.capture());
		assertThat(volume1.getValue(), isGreaterThan(0.1f));
		assertThat(volume2.getValue(), isGreaterThan(0.1f));

		when(music1.isPlaying()).thenReturn(true);
		when(music2.isPlaying()).thenReturn(true);
		system.update(halfCrossFade);
		verify(music1, atLeastOnce()).setVolume(volume1.capture());
		verify(music2, atLeastOnce()).setVolume(volume2.capture());
		assertThat(volume1.getValue(), isCloseTo(0));
		assertThat(volume2.getValue(), isCloseTo(1));
	}

	@Test
	public void playWhenJustStartedFadingPseudoCrossFades() {
		system.play(music1);
		when(music1.isPlaying()).thenReturn(true);
		system.update(longTime);

		system.play(music2);
		when(music2.isPlaying()).thenReturn(true);
		system.update(shortTime);

		system.play(music3);
		when(music3.isPlaying()).thenReturn(true);
		verify(music1, times(1)).play();
		verify(music2, times(1)).stop();
		verify(music3, times(1)).play();
		verify(music1, atLeastOnce()).setVolume(volume1.capture());
		verify(music3, atLeastOnce()).setVolume(volume3.capture());
		assertThat(volume1.getValue(), isGreaterThan(0.5f));
		assertThat(volume3.getValue(), isCloseTo(0));
	}

	@Test
	public void playWhenAlmostFinishedFadingPseudoCrossFades() {
		system.play(music1);
		when(music1.isPlaying()).thenReturn(true);
		system.update(longTime);

		system.play(music2);
		when(music2.isPlaying()).thenReturn(true);
		system.update(underCrossFade);

		system.play(music3);
		when(music3.isPlaying()).thenReturn(true);
		verify(music1, times(1)).stop();
		verify(music2, times(1)).play();
		verify(music3, times(1)).play();
		verify(music2, atLeastOnce()).setVolume(volume2.capture());
		verify(music3, atLeastOnce()).setVolume(volume3.capture());
		assertThat(volume2.getValue(), isGreaterThan(0.5f));
		assertThat(volume3.getValue(), isCloseTo(0));
	}

}
