import keras
import soundfile
import torchaudio
import numpy as np
import librosa
import torch.nn as nn
import torch
import tensorflow as tf
import gc

from nnutils import *

'''
Script for evaluating trained model
'''

gpus = tf.config.experimental.list_physical_devices('GPU')
for gpu in gpus:
  tf.config.experimental.set_memory_growth(gpu, True)

text_transform = TextTransform()

def data_processing(data):
    '''
    Data preprocessing
    '''
    spectrograms = []
    labels = []
    input_lengths = []
    label_lengths = []

    for(waveform, _, utterance, _, _, _) in data:
        spec = torch.from_numpy(
            librosa.feature.melspectrogram(
                waveform.detach().numpy()[0],
                sr=16000,
                n_fft=512,
                hop_length=256,
                n_mels=161
            )
        ).squeeze(0).transpose(0,1)

        spectrograms.append(spec)

        label = utterance.lower()
        labels.append(label)

        input_lengths.append(spec.shape[0])
        label_lengths.append(len(label))

    spectrograms = nn.utils.rnn.pad_sequence(
        spectrograms, batch_first=True
    ).unsqueeze(1).transpose(2,3).transpose(1,3)
    #labels = nn.utils.rnn.pad_sequence(labels, batch_first=True)

    return spectrograms, labels, input_lengths, label_lengths

def decoder(values):
    '''
    Converts output of NN to text
    '''
    decodes = []

    arg_maxes = torch.argmax(torch.tensor(values), dim=2)
    for i, args in enumerate(arg_maxes):
        decode = []
        for j, index in enumerate(args):
            if index != 28:
                if True and j != 0 and index == args[j-1]:
                    continue
                decode.append(index.item())
        decodes.append(text_transform.int_to_text(decode))

    return decodes


model = keras.models.load_model("KerasModel")
#model.load_weights("chck_v3")

model.output_length = lambda x: x

test_set = torchaudio.datasets.LIBRISPEECH("/mnt/LibriSpeech/", url="test-clean", download=False)

test_cer, test_wer = [], []
batch_size = 20

for k in range(1000,len(test_set), batch_size):
    rawData = []
    for n in range(0,batch_size):
        if (k+n) < len(test_set):
            rawData.append(test_set[k+n])

    dataProcessed = data_processing(rawData)
    data = dataProcessed[0]
    correct = dataProcessed[1]

    values = model.predict_on_batch(data)
    decodes = decoder(values)

    print(k)

    for n in range(len(decodes)):
        cc = cer(decodes[n], correct[n])
        ww = wer(decodes[n], correct[n])

        test_cer.append(cc)
        test_wer.append(ww)

    gc.collect()
    tf.keras.backend.clear_session()

    if(k>1040):#There is probably bug in Keras that causes memory leak 
                #therefore evaluation need to be ended up earlier
        break  

print(sum(test_cer)/len(test_cer))
print(sum(test_wer)/len(test_wer))
print(len(test_cer), len(test_wer))

