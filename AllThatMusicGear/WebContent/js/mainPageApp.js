var mainPageApp = angular.module('mainPageApp',[]);

var checkLogin = function () {
	$.get("http://localhost:8080/AllThatMusicGear/UserServlet/GetUserInfo", function(data, status){
		if (data.nickName == "null"){
			window.location.href = '/AllThatMusicGear/login.html';			
		}
    });
	};

mainPageApp.controller('navBarController', ['$scope', '$http', function($scope, $http) {
	$scope.loggedInUserNickName = "Welcome ";
	$scope.loggedInUserPhotoURL = "media/defaultIcon.png";
	$scope.loggedInUserInfo = function(){
		$http.get(
				"http://localhost:8080/AllThatMusicGear/UserServlet/GetUserInfo"
		).success(function(response) {
			if (response.nickName != "null"){
				// redirect to login page
				
				$scope.loggedInUserNickName = "Welcome " + response.nickName + " ";					 
			}
			else {
				window.location.href = '/AllThatMusicGear/login.html';
				return;
			}
			if (response.photoURL != "null" && response.photoURL !=""){
				$scope.loggedInUserPhotoURL = response.photoURL;
			}				 
		})		
	}
	$scope.loggedInUserInfo();
	
	$scope.submitQuestion = function()
		{
			// make sure the user is still logged in
			checkLogin();
			var parameters = {
					params: {
						qText: $scope.qText,
						topicList: $scope.qTopics,						
					}
			};
			$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/InsertQuestion", parameters);
			$scope.resetQFields();
			$('#askQuestionModal').modal('hide');
		}
	
	$scope.resetQFields = function(){
		$scope.qText = "";
		$scope.qTopics = "";
	}			 
 }]);

mainPageApp.controller('NewQuestions', ['$scope', '$http', function($scope, $http) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	
	$scope.updateQuestions = function(){
		$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/AllQuestions")
		.success(function(response) {	
			$scope.newQuestions = response;
			$scope.qNewCounter = 0;
			$scope.maxPageNum = parseInt($scope.newQuestions.length/20) + 1;
			$scope.newQuestions = $scope.newQuestions.slice(($scope.pageNum-1)*20,$scope.pageNum*20)
			for (var i =0;i < $scope.newQuestions.length; i++) {
				var parameters = {
						params: {
							qID: $scope.newQuestions[i].qID,
						}
				};
				$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/AnswersOfQ", parameters)
				.success(function(response) {
					if(response[0] !== undefined){
						for (var j =0;j < $scope.newQuestions.length; j++){
							if ($scope.newQuestions.qID == response.qID){
								$scope.newQuestions[j].answers = response;
							}
						}
					}
				});
			}
		});
	};
	
	$scope.updateQuestions();
	
	$scope.nextPage = function(){
		if ($scope.pageNum < $scope.maxPageNum){
			$scope.pageNum += 1;
			$scope.updateQuestions();
		}
	}
	
	$scope.prevPage = function(){
		if ($scope.pageNum > 1){
			$scope.pageNum -= 1;
			$scope.updateQuestions();
		}	
	}
}]);





