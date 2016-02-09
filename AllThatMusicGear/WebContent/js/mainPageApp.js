var loggedInUser = "null";
var checkLogin = function () {
	$.get("UserServlet/GetSessionInfo", function(data, status){
		if (data.nickName == "null"){
			loggedInUser = data.nickName;
			window.location.href = '/AllThatMusicGear/login.html';			
		}
	});
};

var mainPageApp = angular.module('mainPageApp',[]);

mainPageApp.directive('header', function(){
	return {
	    templateUrl: "/AllThatMusicGear/html-resources/header.html",
	};
});

mainPageApp.directive('footer', function(){
	return {
	    templateUrl: "/AllThatMusicGear/html-resources/footer.html",
	};
});

mainPageApp.directive('questionsthread', function(){
	return {
	    templateUrl: "/AllThatMusicGear/html-resources/questionsthread.html",
	};
});

mainPageApp.controller('navBarController', ['$scope', '$http', function($scope, $http) {
	$scope.loggedInUserInfo = function(){
		$http.get("UserServlet/GetSessionInfo")
		.success(function(response) {
			if (response.nickName != "null"){
				$scope.loggedInUserNickName = response.nickName;	
				$scope.loggedInUserPhotoURL = response.photoURL;
			}
			else {
				window.location.href = '/AllThatMusicGear/login.html';
			}
		})
	}
	$scope.loggedInUserInfo();
	
	$scope.submitQuestion = function()
		{
			checkLogin();
			var parameters = {
					params: {
						qText: $scope.qText,
						topicList: $scope.qTopics,						
					}
			};
			$http.get("QandAServlet/InsertQuestion", parameters);
			$scope.resetQFields();
			$('#askQuestionModal').modal('hide');
		}
	
	$scope.resetQFields = function(){
		$scope.qText = "";
		$scope.qTopics = "";
	}
	
	$scope.logOut = function(){
		$http.post("UserServlet/LogOut");
	}
 }]);

mainPageApp.controller('questions', ['$scope', '$http', '$location',function($scope, $http, $location) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	
	var url = window.location.pathname;
	if (url.search("newquestions") != -1){
		$scope.displayMode = "NewQuestions";
	}
	else {
		$scope.displayMode = "AllQuestions";
	}
	
	
	$scope.updateQuestions = function(){
		$http.get("QandAServlet/" + $scope.displayMode)
		.success(function(response) {
			$scope.questions = angular.copy(response);
			$scope.qNewCounter = 0;
			$scope.maxPageNum = parseInt($scope.questions.length/20) + 1;
			$scope.questions = $scope.questions.slice(($scope.pageNum-1)*20,$scope.pageNum*20)
			for (var i =0;i < $scope.questions.length; i++) {
				var parameters = {
						params: {
							qID: $scope.questions[i].qID,
						}
				};
				$http.get("QandAServlet/AnswersOfQ", parameters)
				.success(function(response) {
					if(response[0] !== undefined){
						for (var j =0;j < $scope.questions.length; j++){
							if ($scope.questions[j].qID == response[0].qID){
								$scope.questions[j].answers = angular.copy(response);
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
	
	$scope.voteQuestion = function(qID, changeScore)
	{
		checkLogin();
		var parameters = { params: { qId: qID, changeVS: changeScore,} };
		$http.get("QandAServlet/UpdateQuestion", parameters)
		.success(function(response) {
			for (var i = 0; i < $scope.questions.length; i++){
				if ($scope.questions[i].qID == qID){
					$scope.questions[i].qVotingScore += changeScore;
					if ($scope.displayMode != "NewQuestions")
					{
						$scope.questions.sort(function(a,b)
								{
							return b.qVotingScore - a.qVotingScore;
								});
					}
					return;
						
					}
			}
			//TODO in server	$scope.updateUserRating(userNickName);
		});
	}
	
	$scope.voteAnswer = function(qID, aID, changeScore)
	{
		checkLogin();
		var parameters = { params: { qId: qID ,aID: aID, changeVS: changeScore,} };
		$http.get("QandAServlet/UpdateAnswer", parameters)
		.success(function(response) {
			for (var i = 0; i < $scope.questions.length; i++){
				if ($scope.questions[i].qID == qID){
					for (var j = 0;j < $scope.questions[i].answers.length; j++){
						if ($scope.questions[i].answers[j].aID == aID){
							$scope.questions[i].answers[j].aVotingScore += changeScore;
							$scope.questions[i].answers.sort(function(a,b)
									{
										return b.aVotingScore - a.aVotingScore;
									});
							return;
						}
					}
				}
			}
			//TODO in server	$scope.updateUserRating(userNickName);
		});
	}
	
	$scope.submitAnswer = function(qID, qText)
	{
		checkLogin();
		var parameters = {
				params: {
					qID: qID,
					aText: qText,
				}
		};
		$http.get("QandAServlet/InsertAnswer", parameters)
		.success(function(response) {
			//First of all updating the questing Rating
			// TODO-server should be done on server $scope.updateQuestionDueToAnswerChange($scope.qToAnser);
			//now updated the answer submiter Rating 
			// TODO-server $scope.updateUserRating($scope.logedInUser);
			var parameters = {
					params: {
						qID: qID,
					}
			};
			$http.get("QandAServlet/AnswersOfQ", parameters)
			.success(function(response) {
				if(response[0] !== undefined){
					for (var i = 0; i < $scope.questions.length; i++){
						if ($scope.questions[i].qID == qID){
							$scope.questions[i].answers = response;
						}
					}
				}
			});
			
		});
	}
	
}]);

mainPageApp.controller('userProfile', ['$scope', '$http', '$location', function($scope, $http, $location) {
	$scope.userNickname = $location.hash();
	
	var parameters = {
			params: {
				userNickName: $scope.userNickname,
			}};
	
	$http.get("UserServlet/GetUserInfo", parameters)
	.success(function(response) {
		$scope.userInfo = response;
	});
//	TODO: EXPERTISE doesn't work
//	$http.get("http://localhost:8080/AllThatMusicGear/UserServlet/UserExpertise", parameters)
//	.success(function(response) {
//		$scope.userInfo = response;
//	});
	$http.get("QandAServlet/UserLastAnswerdAnswers", parameters)
	.success(function(response) {
		$scope.userInfo = response;
	});
	
}]);

mainPageApp.controller('leaderboardCtrl', ['$scope', '$http', function($scope, $http) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	
	$scope.updateLeaderboard = function(){
		$http.get("UserServlet/GetAllUserInfo")
		.success(function(response){
			$scope.allUsers = response;
			$scope.maxPageNum = parseInt($scope.allUsers.length/20) + 1;
			$scope.allUsers = $scope.allUsers.slice(($scope.pageNum-1)*20,$scope.pageNum*20)
		});
	};
	
	$scope.updateLeaderboard();
	
	$scope.nextPage = function(){
		if ($scope.pageNum < $scope.maxPageNum){
			$scope.pageNum += 1;
			$scope.updateLeaderboard();
		}
	}
	
	$scope.prevPage = function(){
		if ($scope.pageNum > 1){
			$scope.pageNum -= 1;
			$scope.updateLeaderboard();
		}	
	}
}]);
