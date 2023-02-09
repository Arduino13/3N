from nnutils import *
from speechModelPytorch import *

text_transform = TextTransform()

def data_processing(data):
    '''
    Preprocessing of data
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

        label = torch.Tensor(text_transform.text_to_int(utterance.lower()))
        labels.append(label)

        input_lengths.append(spec.shape[0])
        label_lengths.append(len(label))

    spectrograms = nn.utils.rnn.pad_sequence(
        spectrograms, batch_first=True
    ).unsqueeze(1).transpose(2,3)
    labels = nn.utils.rnn.pad_sequence(labels, batch_first=True)

    return spectrograms, labels, input_lengths, label_lengths


kwargs = {'num_workers':1, 'pin_memory': True}

test_dataset = torchaudio.datasets.LIBRISPEECH("/mnt/LibriSpeech", url='test-other', download=False)
test_loader = data.DataLoader(dataset=test_dataset,
                                batch_size=20,
                                shuffle=False,
                                collate_fn=lambda x: data_processing(x))

import numpy as np

model = SpeechModel().to('cuda')
#model.load_state_dict(torch.load('/mnt/LibriSpeech/2020MODELPYTORCH/model.txt'))
model.eval()

with torch.no_grad():
    test_loss = 0
    test_cer, test_wer = [], []

    for (i, _data) in enumerate(test_loader):
            print(i)
            spectrograms, labels, input_lengths, label_lengths = _data
            spectrograms, labels = spectrograms.to('cuda'), labels.to('cuda')

            output = model(spectrograms)
            output = F.log_softmax(output, dim=2)
            output = output.transpose(0, 1)

            decoded_preds, decoded_targets = GreedyDecoder(output.transpose(0, 1), labels, label_lengths)
            for j in range(len(decoded_preds)):
                pass
                test_cer.append(cer(decoded_targets[j], decoded_preds[j]))
                test_wer.append(wer(decoded_targets[j], decoded_preds[j]))


    avg_cer = sum(test_cer)/len(test_cer)
    avg_wer = sum(test_wer)/len(test_wer)

    print('Test set: Average loss: {:.4f}, Average CER: {:4f} Average WER: {:.4f}\n'.format(test_loss, avg_cer, avg_wer))

