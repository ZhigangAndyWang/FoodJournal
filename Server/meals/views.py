# Create your views here.
import json,os
from time import time
from django import forms
from django.contrib.auth.decorators import login_required
from django.contrib.auth.views import login
from django.contrib.auth import logout,authenticate
from django.http import HttpResponseRedirect, HttpResponse
from django.shortcuts import render_to_response,redirect,render,get_object_or_404
#from registration.forms import RegistrationForm
#from django.contrib.auth.forms import UserCreationForm
from django.contrib.auth.models import User, Permission
from forms import UserCreateForm,UploadFileForm
from meals.models import Food, Comment
from django.http import Http404
from django.template import RequestContext, loader
from django.core.context_processors import csrf
from foodjournal.settings import MEDIA_ROOT

returnJson = 0;

def index(request):
    global returnJson
    if request.user_agent.is_mobile:
        returnJson = 1
    else:
        returnJson = 0
    #if request.method == 'GET':
    #    print 'get'
    #elif request.method == 'POST':
    #    print 'post'       
    user =  request.user
    data = {'foo': 'bar', 'hello': 'world'}
    if returnJson == 1:
        return HttpResponse(json.dumps(data), content_type='application/json')
    elif returnJson == 0:
        return render(request,'index.html')


def customregister(request):
    global returnJson
    if request.user_agent.is_mobile:
        returnJson = 1
    else:
        returnJson = 0
    data_dict = {}
    data_dict['type'] = 'register'
    if request.method == 'POST':
        form = UserCreateForm(request.POST)
        if form.is_valid():
            new_user = form.save()
            data_dict['success'] = 1
            data_dict['uid'] = new_user.id
            data_dict['username'] = new_user.username
            data_dict['name'] = new_user.first_name + new_user.last_name
            data_dict['email'] = new_user.email

            #Redirect to the registration complete page
            #return HttpResponseRedirect("/homepage/")
        else:
            data_dict['success'] = 0
            #invalid form
            data_dict['error_message'] = 'invalid registration form'
        if returnJson == 1:
            return HttpResponse(json.dumps(data_dict),content_type='application/json')
        elif returnJson == 0:
            return redirect('/accounts/register')
    else:
        form = UserCreateForm()
        return render(request, "registration/registration_form.html", {
            'form': form,
        })

    #data_dict = {}
    #if request.method == 'POST':
    #    data_dict['type'] = 'register'
    #    data_dict['success'] = request.user.is_authenticated
    #    data_dict['uid'] = request.user.id
    #    data_dict['username'] = request.user.username
    #    data_dict['name'] = request.user.first_name + request.user.last_name
    #    data_dict['email'] = request.user.email
    #return HttpResponse(json.dumps(data_dict),content_type='application/json')


def customlogin(request):
    global returnJson
    if request.user_agent.is_mobile:
        returnJson = 1
    else:
        returnJson = 0
    if request.method == 'POST':
        data_dict = {}
        data_dict['type'] = 'login'
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(username=username, password=password)
        if user is not None:
            data_dict['uid'] = user.id
            data_dict['username'] = user.username
            data_dict['name'] = user.first_name + user.last_name
            data_dict['email'] = user.email
            if user.is_active:
                # Redirect to a success page.
                data_dict['success'] = 1 
            else:
                # Return a 'disabled account' error message
                data_dict['success'] = 0
                data_dict['error_message'] = 'disabled account'
        else:
            data_dict['success'] = 0 
            data_dict['error_message'] = 'username and password not match'
        if returnJson == 1:
            return HttpResponse(json.dumps(data_dict),content_type='application/json')
        elif returnJson == 0:
            return login(request)

    return login(request)

def uploadImage(request):
    global returnJson
    if request.user_agent.is_mobile:
        returnJson = 1
    else:
        returnJson = 0
    data_dict = {}
    if request.method == 'POST':
        form = UploadFileForm(request.POST, request.FILES)
        if form.is_valid():
            handle_uploaded_file(request)
            data_dict['success'] = 1 
        else:
            data_dict['success'] = 0 
            data_dict['error_message'] = 'upload error'
        if returnJson == 1:
            return HttpResponse(json.dumps(data_dict),content_type='application/json')
        elif returnJson == 0:
            #TODO SUCCESS PAGE
            return redirect('/uploadImage/')
    else:
        form = UploadFileForm()
    return render_to_response('uploadImage.html', {'form': form})

def handle_uploaded_file(request):
    user = User.objects.get(username=request.POST['username'])
    username = user.username
    f = request.FILES['file']
    file_path = generate_filepath(username,f.name)
    #write the image file
    with open(file_path, 'wb+') as destination:
        for chunk in f.chunks():
            destination.write(chunk)
    #bind the auth.User and meal.Food image database
    #new_food = Food.objects.create(user = user, image = file_path[14:], description = request.POST['description'])
    new_food = Food.objects.create(user = user, image = file_path[20:], description = request.POST['description'])
    new_food.save()

def getImages(request,userName):
    global returnJson
    if request.user_agent.is_mobile:
        returnJson = 1
    else:
        returnJson = 0
    user = User.objects.get(username=userName)
    fs = user.food_set.all()

    #if returnJson == 1:
    data_dict = {}
    images_list = []
    for f in fs:
        image_dict = {}
        image_dict['imageUrl'] = f.image.url
        image_dict['description'] = f.description
        image_dict['created_date'] = str(f.pub_date)
        images_list.append(json.dumps(image_dict))

    data_dict['images'] = images_list
    if returnJson == 1:
        return HttpResponse(json.dumps(data_dict),content_type='application/json')

    elif returnJson == 0:
        return render(request,'myMeal_details.html',{'fs':fs})

def generate_filepath(username, name):
    ext = name.split('.')[-1]
    directory = './foodjournal/media/food_images/' +username + '/'
    filename = str(int(time()))+'_'+ name
    if not os.path.exists(directory):
        os.makedirs(directory)
    return directory+filename


@login_required
def homepage(request):
    #c = {}
    #c.update(csrf(request))

    user = request.user
    username = user.username
    url = '/getImages/username='+username
    return redirect(url)
