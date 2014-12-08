from django.db import models
from django.contrib.auth.models import User
from django.shortcuts import get_object_or_404, render
import datetime
from thumbs import ImageWithThumbsField 

# Create your models here.

class FriendList(models.Model):
    owner = models.OneToOneField(User,related_name="friend_list")
    friends = models.ManyToManyField(User,related_name = "followed_list")


class Food(models.Model):
    user = models.ForeignKey(User)
    name = models.CharField(max_length=200,default='')
    pub_date = models.DateTimeField('date published',default=datetime.datetime.now(),null=True)
    location = models.CharField(max_length=200,default='NY')
    #pic_url = models.CharField(max_length=511,default='sample.png',null=True)
    image = models.ImageField(upload_to="food_images/")
    description = models.CharField(max_length=4000,null=True)
    c_tag = models.ForeignKey('CusineTag',null=True)
    t_tag = models.ForeignKey('TypeTag',null=True)

    def __str__(self):
        return self.image.url
    
class Comment(models.Model):
    text = models.CharField(max_length=4000)
    poster = models.ForeignKey(User)
    food = models.ForeignKey(Food,null=True)
    created_on = models.DateTimeField(null=True)

    def __str__(self):
        return self.text

class CusineTag(models.Model):
    name = models.CharField(max_length=200)
    creator = models.ForeignKey(User,null=True)

    def __str__(self):
        return self.name

class TypeTag(models.Model):
    name = models.CharField(max_length=200)
    creator = models.ForeignKey(User,null=True)

    def __str__(self):
        return self.name
