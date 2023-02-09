'''
Converts model to mobile version
'''
import tensorflow as tf

converter = tf.lite.TFLiteConverter.from_saved_model('KerasModel')
converter.target_spec.supported_ops = [
    tf.lite.OpsSet.TFLITE_BUILTINS, # enable TensorFlow Lite ops.
    tf.lite.OpsSet.SELECT_TF_OPS # enable TensorFlow ops.
]
tflite_model = converter.convert()

with open('model_mobile.tflite', 'wb') as f:
    f.write(tflite_model)
