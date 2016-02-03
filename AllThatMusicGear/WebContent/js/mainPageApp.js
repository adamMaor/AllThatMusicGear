var mainPageApp = angular.module('mainPageApp',[]);
mainPageApp.controller('mainPageAppController', ['$scope', '$http', '$window', function($scope, $http, $window) {
	$scope.loggedInUserNickName = "Welcome Guest ";
	$scope.loggedInUserPhotoURL = "media/defaultIcon.png";
	
	$scope.loggedIn = function(){
		$http.get(
				"http://localhost:8080/AllThatMusicGear/UserServlet/GetUserInfo"
		).success(function(response) {
			if (response.nickName != "null"){
				// redirect to login page
				
				$scope.loggedInUserNickName = "Welcome " + response.nickName + " ";					 
			}
			else {
				$window.location.href = '/AllThatMusicGear/login.html';
				return;
			}
			if (response.photoURL != "null" && response.photoURL !=""){
				$scope.loggedInUserPhotoURL = response.photoURL;
			}				 
		})		
	}
	$scope.loggedIn();
	
	$scope.submitQuestion = function()
		{
			// make sure the user is still logged in
			$scope.loggedIn();
			var parameters = {
					params: {
						qText: $scope.qText,
						topicList: $scope.qTopics,						
					}
			};
			debugger;
			$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/InsertQuestion", parameters);
			$scope.resetQFields();
			$('#askQuestionModal').modal('hide');
		}
	
	$scope.resetQFields = function()
	{
		debugger;
		$scope.qText = "";
		$scope.qTopics = "";
	}
			 
 }]);