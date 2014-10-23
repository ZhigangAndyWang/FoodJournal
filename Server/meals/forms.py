from django import forms
from django.contrib.auth.models import User
from django.contrib.auth.forms import UserCreationForm

class UserCreateForm(UserCreationForm):
    email = forms.EmailField(required=True)
    #first_name = forms.CharField(required=False)

    class Meta:
        model = User
        fields = ("username", "email", "password1", "password2")

    def save(self, commit=True):
        user = super(UserCreateForm, self).save(commit=False)
        user.email = self.cleaned_data["email"]
        if commit:
            user.save()
        return user

class UploadFileForm(forms.Form):
    description = forms.CharField(max_length=4000)
    file = forms.FileField()
    username = forms.CharField(max_length=1023)

    location = forms.CharField(max_length=4000,required=False)
    tags = forms.CharField(max_length=1023,required=False)
