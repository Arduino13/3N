import torch
import torch.nn as nn
import torchaudio
import torch.utils.data as data
import torch.optim as optim
import torch.nn.functional as F
import numpy as np
import torch.utils.mobile_optimizer as mobile_optimizer
import librosa

class BidirectionalGRU(nn.Module):
    def __init__(self, rnn_dim, hidden_size, dropout, batch_first):
        super(BidirectionalGRU, self).__init__()

        self.BiGRU = nn.GRU(
            input_size=rnn_dim, hidden_size=hidden_size,
            num_layers=1, batch_first=batch_first, bidirectional=True)
        self.layer_norm = nn.LayerNorm(rnn_dim)
        self.dropout = nn.Dropout(dropout)

    def forward(self, x):
        x = self.layer_norm(x)
        x = F.gelu(x)
        x, _ = self.BiGRU(x)
        x = self.dropout(x)
        return x

class CNNLayerNorm(nn.Module):
    '''
    Layer normalization for CNN
    '''
    def __init__(self, n_feats):
        super(CNNLayerNorm, self).__init__()
        self.layer_norm = nn.LayerNorm(n_feats)

    def forward(self, x):
        # x (batch, channel, feature, time)
        x = x.transpose(2, 3).contiguous() # (batch, channel, time, feature)
        x = self.layer_norm(x)
        return x.transpose(2, 3).contiguous() # (batch, channel, feature, time)

class CNNLayerNorm1D(nn.Module):
    '''
    Layer normalization for 1D CNN
    '''
    def __init__(self, n_feats):
        super(CNNLayerNorm1D, self).__init__()
        self.layer_norm = nn.LayerNorm(n_feats)

    def forward(self, x):
        x = x.transpose(1,2).contiguous()
        x = self.layer_norm(x)
        return x.transpose(1,2).contiguous()

class CNN(nn.Module):
   def __init__(self, n_channels_in, n_channels_out, kernel, stride, dropout=0.1, n_mels = 161):
        super(CNN, self).__init__()

        self.cnn = nn.Conv2d(n_channels_in, n_channels_out, kernel, stride, padding=1)
        self.dropout = nn.Dropout(dropout)
        self.norm = CNNLayerNorm(n_mels)

   def forward(self, x):
        x = self.cnn(x)
        x = F.gelu(x)
        x = self.dropout(x)
        x = self.norm(x)
        return x

class CNN1D(nn.Module):
    def __init__(self, n_channels_in, n_channels_out, kernel, stride, dropout=0.1, n_mels = 161):
        super(CNN1D, self).__init__()

        self.cnn = nn.Conv1d(n_channels_in, n_channels_out, kernel, stride, padding=1)
        self.dropout = nn.Dropout(dropout)
        self.norm = CNNLayerNorm1D(n_channels_out)

    def forward(self, x):
        x = self.cnn(x)
        x = F.gelu(x)
        x = self.dropout(x)
        x = self.norm(x)
        return x

#taken from: https://discuss.pytorch.org/t/any-pytorch-function-can-work-as-keras-timedistributed/1346/4
class TimeDistributed(nn.Module):
    '''
    Special type of layer which can apply another layer along time axis to each
    timestamp individualy
    '''
    def __init__(self, module, batch_first=True):
        super(TimeDistributed, self).__init__()
        self.module = module
        self.batch_first = batch_first

    def forward(self, x):

        if len(x.size()) <= 2:
            return self.module(x)

        # Squash samples and timesteps into a single axis
        x_reshape = x.contiguous().view(-1, x.size(-1))  # (samples * timesteps, input_size)

        y = self.module(x_reshape)

        # We have to reshape Y
        if self.batch_first:
            y = y.contiguous().view(x.size(0), -1, y.size(-1))  # (samples, timesteps, output_size)
        else:
            y = y.view(-1, x.size(1), y.size(-1))  # (timesteps, samples, output_size)

        return y

class SpeechModel(nn.Module):
    '''
    Main architecture of the nework in PyTorch
    '''
    def __init__(self):
        super(SpeechModel, self).__init__()

        self.first_cnn = CNN(1,16,3,1)
        self.second_cnn = CNN(16,16,3,1)

        self.first_dense = nn.Linear(2576, 200)

        self.cnn_1D = CNN1D(200,200,3,1)

        self.second_dense = nn.Linear(200,200)

        self.RNN = self.birnn_layers = nn.Sequential(*[
            BidirectionalGRU(
                rnn_dim=200 if i==0 else 400, #first RNN layer is smaller, so it fits output dimensions of CNN
                hidden_size=200,
                batch_first=True,
                dropout=0.1
            )
            for i in range(4)
        ])

        self.result = nn.Sequential(
            nn.Linear(400, 200),
            nn.GELU(),
            nn.Dropout(0.1),
            TimeDistributed(nn.Linear(200, 29))
        )

    def forward(self, x):
        x = self.first_cnn(x)
        x = self.second_cnn(x)

        sizes = x.size()
        x = x.view(sizes[0], sizes[1]*sizes[2], sizes[3])
        x = x.transpose(1,2)

        x = self.first_dense(x)
        x = F.gelu(x)

        x = x.transpose(1,2)
        x = self.cnn_1D(x)
        x = x.transpose(1,2)

        x = self.second_dense(x)
        x = F.gelu(x)

        x = self.birnn_layers(x)

        x = self.result(x)
        return x

