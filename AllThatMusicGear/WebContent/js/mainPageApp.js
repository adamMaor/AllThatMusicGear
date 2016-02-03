var mainPageApp = angular.module('mainPageApp',[]);
mainPageApp.controller('mainPageAppController', ['$scope', '$http', function($scope, $http) {
	 $scope.loggedInUserNickName = "Welcome Guest ";
	 $scope.loggedInUserPhotoURL = "media/defaultIcon.png";
	 $http.get(
			 "http://localhost:8080/AllThatMusicGear/UserServlet/GetUserInfo"
			 ).success(function(response) {
				 if (response.nickName != "null"){
					 // redirect to login page
					 
					 $scope.loggedInUserNickName = "Welcome " + response.nickName + " ";					 
				 }
				 if (response.photoURL != "null"){
					 $scope.loggedInUserPhotoURL = response.photoURL;					 
				 }				 
			 })
 }]);