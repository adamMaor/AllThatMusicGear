var loggedInUser = "null";
var checkLogin = function () {
	$.get("UserServlet/GetSessionInfo", function(data, status){
		if (data.nickName == "null"){		
			window.location.href = '/AllThatMusicGear/login.html';		
		}
		else{
			loggedInUser = jQuery.parseJSON(data).nickName;
		}
	});
};

var mainPageApp = angular.module('mainPageApp',[]);

window.onhashchange = function() {
	window.location.reload();
};

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
	$scope.Topic = "";
	
	var url = window.location.pathname;
	if (url.search("newquestions") != -1){
		$scope.questionMode = "NewQuestions";
		$scope.Title = "New Questions:";
	}
	else if (url.search("topquestions") != -1){
		$scope.questionMode = "AllQuestions";
		$scope.Title = "Top Questions:";
	}
	else {
		$scope.Topic = $location.hash();
		$scope.questionMode = "QuestionsByTopic";
		$scope.Title = "Questions By Topic - " + $scope.Topic;
	}
	var parameter = { params: { topic: $scope.Topic, pageNum: $scope.pageNum,} };
	
	$scope.updateQuestions = function(){
		$http.get("QandAServlet/" + $scope.questionMode, parameter)
		.success(function(response) {
			$scope.questions = response.questions;				
			var totalQustions = parseInt(response.numQuestion);
			$scope.maxPageNum = parseInt(totalQustions/20 + 1);
			debugger;
		});
	};
	
	$scope.updateQuestions();
	
	$scope.nextPage = function(){
		if ($scope.pageNum < $scope.maxPageNum){
			$scope.pageNum += 1;
			parameter = { params: { topic: $scope.Topic, pageNum: $scope.pageNum,} };
			$scope.updateQuestions();
		}
	}
	
	$scope.prevPage = function(){
		if ($scope.pageNum > 1){
			$scope.pageNum -= 1;
			parameter = { params: { topic: $scope.Topic, pageNum: $scope.pageNum,} };
			$scope.updateQuestions();
		}	
	}
	
	$scope.voteQuestion = function(qID, changeScore, event)
	{
		checkLogin();
		var elem = event.currentTarget;
		
		var parameters = { params: { qId: qID, changeVS: changeScore,} };
		$http.get("QandAServlet/UpdateQuestion", parameters)
		.success(function(response) {
			if (response != undefined && response.failed){
				$scope.registerPopOver(false, elem, response.error);
			}
			else {
				$scope.registerPopOver(true, elem, "");
				for (var i = 0; i < $scope.questions.length; i++){
					if ($scope.questions[i].qst,qID == qID){
						$scope.questions[i].qst.qVotingScore += changeScore;
					}
				}
			}
			
			// show the popover with the response
			$(elem).popover({title:$scope.qtitle ,content: $scope.qtext, trigger: "hover"});
			$(elem).popover("show");
		});
	}
	
	$scope.voteAnswer = function(qID, aID, changeScore, event)
	{
		checkLogin();
		var elem = event.currentTarget;
		
		var parameters = { params: { qID: qID ,aID: aID, changeVS: changeScore,}};
		$http.get("QandAServlet/UpdateAnswer", parameters)
		.success(function(response) {
			if (response != undefined && response.failed){
				$scope.registerPopOver(false, elem, response.error);
			}
			else {
				$scope.registerPopOver(true, elem, "");
				for (var i = 0; i < $scope.questions.length; i++){
					if ($scope.questions[i].qst.qID == qID){
						for (var j = 0;j < $scope.questions[i].ans.length; j++){
							if ($scope.questions[i].ans[j].aID == aID){
								$scope.questions[i].ans[j].aVotingScore += changeScore;
								$scope.questions[i].ans.sort(function(a,b)
										{
											return b.aVotingScore - a.aVotingScore;
										});
							}
						}
					}
				}
			}
		});
	}
	
	$scope.registerPopOver = function (success, elem, errorText){		
		if (success){
			$scope.title = "Success";
			$scope.text = "Vote registered";
			$(elem).removeClass('btn-default').addClass('btn-primary');
		}
		else {
			$scope.title = "Vote Failed";
			$scope.text = errorText;			
		}
		$(elem).popover({title:$scope.title ,content: $scope.text, trigger: "hover"});
		$(elem).popover("show");
	};
	
	$scope.submitAnswer = function(qID, aText)
	{
		checkLogin();
		var parameters = {
				params: {
					qID: qID,
					aText: aText,
				}
		};
		$http.get("QandAServlet/InsertAnswer", parameters)
		.success(function(response) {
			var parameters = {
					params: {
						qID: qID,
					}
			};
			$http.get("QandAServlet/AnswersOfQ", parameters)
			.success(function(response) {
				if(response[0] !== undefined){
					for (var i = 0; i < $scope.questions.length; i++){
						if ($scope.questions[i].qst.qID == qID){
							$scope.questions[i].ans = response;
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
		$scope.userInfo = angular.copy(response);
//		alert(response);
	});
//	TODO: EXPERTISE doesn't work
//	$http.get("http://localhost:8080/AllThatMusicGear/UserServlet/UserExpertise", parameters)
//	.success(function(response) {
//		$scope.userInfo = response;
//	});
	$http.get("QandAServlet/UserLastAnswerdAnswers", parameters)
	.success(function(response) {
		$scope.userInfo.lastAnswered = angular.copy(response);
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

mainPageApp.controller('questionsByTopic', ['$scope', '$http', function($scope, $http) {
	
}]);

mainPageApp.controller('topicsCtrl', ['$scope', '$http', function($scope, $http) {
	
}]);
