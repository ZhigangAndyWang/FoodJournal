from meals.models import * 
from textblob.classifiers import NaiveBayesClassifier
from textblob import TextBlob

def get_cl():
    train = build_train_data()
    cl = NaiveBayesClassifier(train)
    return cl

def build_train_data():
    train = []
    all_foods = Food.objects.all().exclude(description__isnull=True)
    for f in all_foods:
        item = (f.description,f.c_tag.name)
        train.append(item)
    return train

def classify_tags(desc,cl):
    tag = cl.classify(desc)
    return tag 

# Show 5 most informative features
#cl.show_informative_features(5)
