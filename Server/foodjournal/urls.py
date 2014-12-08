from django.conf.urls import patterns, include, url
from django.conf import settings

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'foodjournal.views.home', name='home'),
    # url(r'^foodjournal/', include('foodjournal.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    #url('', include('django.contrib.auth.urls')),
    url(r'^admin/', include(admin.site.urls)),
    url(r'^$','meals.views.index',name='index'),
    url(r'^index/','meals.views.index',name='index'),
    url(r'^accounts/', include('registration.backends.default.urls')),
    url(r'^login/$', 'meals.views.customlogin',name="customizedlogin"),
    #url(r'^twitterlogin/$', 'twitterauth.views.twitter_signin',name="twitter_signin"),
    #url(r'^twitterreturn/$', 'twitterauth.views.twitter_return',name="twitter_return"),
    url(r'',include('social_auth.urls')),
    url(r'^logout/$', 'django.contrib.auth.views.logout',
                                  {'next_page': '/index/'}),
    #url(r'^register/$', 'backend': 'registration.backends.default.DefaultBackend',name="customizedregister"),
    url(r'^register/$', 'meals.views.customregister',name="customizedregister"),
    url(r'^homepage/','meals.views.homepage',name='homepage'),
    url(r'^uploadImage/','meals.views.uploadImage',name='uploadimage'),
    url(r'^getImages/username=(?P<userName>\w+)/$','meals.views.getImages',name='getimage'),

    url(r'^recommend/username=(?P<userName>\w+)/$','meals.views.recommend',name='recommend'),
    url(r'^tags/tag=(?P<tag>\w+)/$','meals.views.tags',name='tags'),

    url(r'^getFriends/username=(?P<userName>\w+)/$','meals.views.getFriends',name='getFriends'),
)

if settings.DEBUG:  
    urlpatterns += patterns('', url(r'^media/(?P<path>.*)$', 'django.views.static.serve', {'document_root': settings.MEDIA_ROOT }),   
            url(r'^static/(?P<path>.*)$','django.views.static.serve',{'document_root':settings.STATIC_ROOT}), )  
