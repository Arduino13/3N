'''
Converts model to mobile version
'''

from speechModelPytorch import *

model = SpeechModel().to('cpu')
model.load_state_dict(torch.load('/mnt/LibriSpeech/model.txt'), strict=False)

model = torch.quantization.quantize_dynamic(
    model,
    {torch.nn.Linear, torch.nn.RNN},
    dtype=torch.qint8) #model quantization

tracedModel = torch.jit.script(model)
tracedModel.save('mobile_model.pt')
