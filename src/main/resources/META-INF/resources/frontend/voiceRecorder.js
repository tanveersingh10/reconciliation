let recorder;
let audioChunks = [];
console.log("here");

window.startRecording = function startRecording() {
        console.log("Start Recording Pressed");
        navigator.mediaDevices.getUserMedia({ audio: true })
            .then(stream => {
                recorder = new RecordRTC(stream, {
                    type: 'audio',
                    mimeType: 'audio/wav',
                    recorderType: StereoAudioRecorder,
                    numberOfAudioChannels: 1,
                    desiredSampRate: 16000,
                    timeSlice: 1000, // save every 1000 milliseconds
                    ondataavailable: function(blob) {
                        audioChunks.push(blob);
                    }
                });
                recorder.startRecording();
            }).catch(err => console.error('Error accessing microphone:', err));
    }

window.stopRecording = function stopRecording() {
            console.log("Stop Recording Pressed");
            recorder.stopRecording(() => {
                const blob = new Blob(audioChunks, { type: 'audio/wav' });
                const reader = new FileReader();
                reader.readAsDataURL(blob);
                reader.onloadend = function() {
                    const base64AudioMessage = reader.result.split(',')[1];
                    console.log(base64AudioMessage);
                    this.$server.serverSideCallback(base64AudioMessage);
                };
            });
        };