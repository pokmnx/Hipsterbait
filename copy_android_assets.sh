#!/bin/bash

rm ~/HipsterBait/app/src/main/res/drawable-hdpi/*
rm ~/HipsterBait/app/src/main/res/drawable-mdpi/*
rm ~/HipsterBait/app/src/main/res/drawable-xhdpi/*
rm ~/HipsterBait/app/src/main/res/drawable-xxhdpi/*
rm ~/HipsterBait/app/src/main/res/drawable-xxxhdpi/*

cp ~/Documents/HBAssetsAndroid/hdpi/* ~/HipsterBait/app/src/main/res/drawable-hdpi/
cp ~/Documents/HBAssetsAndroid/mdpi/* ~/HipsterBait/app/src/main/res/drawable-mdpi/
cp ~/Documents/HBAssetsAndroid/xhdpi/* ~/HipsterBait/app/src/main/res/drawable-xhdpi/
cp ~/Documents/HBAssetsAndroid/xxhdpi/* ~/HipsterBait/app/src/main/res/drawable-xxhdpi/
cp ~/Documents/HBAssetsAndroid/xxxhdpi/* ~/HipsterBait/app/src/main/res/drawable-xxxhdpi/
