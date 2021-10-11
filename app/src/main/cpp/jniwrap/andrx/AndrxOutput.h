#ifndef ANDRX_OUTPUT_H
#define ANDRX_OUTPUT_H

#include "IAudioSource.h"
#include "SoundEngine.h"

extern "C" {
#include "andrx.h"
}

class AndrxOutput : public IAudioSource {

public:

    ~AndrxOutput() = default;

    float *pcm;

    static int get_PCM_frame(SoundEngine.getSession(), SoundEngine.getDecoder(), pcm);

    void fillFrame(float *audioData) override {
            audioData = pcm;
        }
};

#endif