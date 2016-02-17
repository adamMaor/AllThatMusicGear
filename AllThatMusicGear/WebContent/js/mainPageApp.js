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
	
	$(document).ready(function(){
	    $('[data-toggle="popover"]').popover(); 
	});
	
	var location = window.location.pathname;
	if (location.toLocaleString().indexOf("leaderboard", 16) != -1){
		$("#leaderNav").addClass( "active" );
	}
	else if (location.toLocaleString().indexOf("profile", 16) != -1){
		$("#profileNav").addClass( "active" );
	}
	else{
		var hash = window.location.hash;
		if (location.toLocaleString().indexOf("topic", 16) != -1 || hash.toLocaleString().indexOf("topic", 0) != -1){
			$("#topicsNav").addClass( "active" );
		}
		else if(hash.toLocaleString().indexOf("newquestions", 0) != -1){
			$("#newQNav").addClass( "active" );
		}
		else if(hash.toLocaleString().indexOf("topquestions", 0) != -1){
			$("#allQNav").addClass( "active" );
		}
		
	}
	
	
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
	
	var url = $location.path();
	if (url == "/newquestions"){
		$scope.questionMode = "NewQuestions";
		$scope.Title = "New Questions:";
		setInterval(function(){$scope.updateQuestions();}, 5000);
	}
	else if (url == "/topquestions"){
		$scope.questionMode = "AllQuestions";
		$scope.Title = "Top Questions:";
	}
	else {
		$scope.Topic = $location.hash();
		$scope.questionMode = "QuestionsByTopic";
		$scope.Title = "Questions By Topic - " + $scope.Topic;
	}
	var parameter = { params: { topic: $scope.Topic, pageNum: $scope.pageNum,} };
	$scope.questions = [];
	
	$scope.updateQuestions = function(){
		$http.get("QandAServlet/" + $scope.questionMode, parameter)
		.success(function(response) {
			if($scope.questions.length != 0){
				if($scope.questions[0].qst.qID == response.questions[0].qst.qID){
					return;
				}
			}
			$scope.questions = response.questions;				
			var totalQustions = parseInt(response.numQuestion);
			$scope.maxPageNum = parseInt((totalQustions-1)/20) + 1;
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
				$scope.registerPopOver(false, elem, response.error, changeScore);
			}
			else {
				$scope.registerPopOver(true, elem, "", changeScore);
				for (var i = 0; i < $scope.questions.length; i++){
					if ($scope.questions[i].qst.qID == qID){
						$scope.questions[i].qst.qVotingScore += changeScore;
					}
				}
			}
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
				$scope.registerPopOver(false, elem, response.error, changeScore);
			}
			else {
				$scope.registerPopOver(true, elem, "", changeScore);
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
	
	$scope.registerPopOver = function (success, elem, errorText, VS){		
		if (success){
			$scope.title = "Success";
			$scope.text = "Vote registered";
			if (VS > 0){
				$(elem).removeClass('btn-default').addClass('btn-primary');				
			}
			else{
				$(elem).removeClass('btn-default').addClass('btn-danger');
			}
		}
		else {
			$scope.title = "Vote Failed";
			$scope.text = errorText;			
		}
		$(elem).popover({title:$scope.title ,content: $scope.text, trigger: "hover"});
		$(elem).popover("show");
	};
	
	// decide on the class of button each button will get
	$scope.buttonClass = function (voteVal, upVoteButton){
		switch(voteVal){
			case -2:
				return 'btn btn-disabled';
			break;
			case -1:
				if (upVoteButton){
					return 'btn btn-disabled';
				}
				else{
					return 'btn btn-danger';					
				}
				break;
			case 0:
				return 'btn btn-default';
				break;
			case 1:
				if (upVoteButton){
					return 'btn btn-primary';
				}
				else{
					return 'btn btn-disabled';					
				}
			break;
		}
	}
	
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

mainPageApp.directive('voteButton', function ($compile) {
    return function (scope, element, attrs) {
    	var id = element[0].id;
    	var buttonClass;
    	if (id =="voteQuestionUp"){
    		buttonClass = scope.buttonClass(scope.qstn.qst.loggedUserVote, true);
    	}
    	else if(id == "voteQuestionDown"){
    		buttonClass = scope.buttonClass(scope.qstn.qst.loggedUserVote, false);
    	}
    	else if (id == "voteAnswerUp"){
    		buttonClass = scope.buttonClass(scope.anwr.loggedUserVote, true);
    	}
    	else if (id == "voteAnswerDown"){
    		buttonClass = scope.buttonClass(scope.anwr.loggedUserVote, false);
    	}
    	
    	$(element).addClass(buttonClass);
    	if (buttonClass.indexOf("disabled") > -1 
    			|| buttonClass.indexOf("primary") > -1 
    			|| buttonClass.indexOf("danger") > -1){
    		element.attr("disabled", "disabled");
    	}
    };
});

mainPageApp.controller('userProfile', ['$scope', '$http', '$location', function($scope, $http, $location) {
	$scope.userNickname = $location.hash();
	
	var parameters = {
			params: {
				userNickName: $scope.userNickname,
			}};
	
	$http.get("UserServlet/GetUserInfo", parameters)
	.success(function(response) {
		$scope.user = angular.copy(response);
	});
	$http.get("QandAServlet/UserLastAskedQuestions", parameters)
	.success(function(response) {
		$scope.user.lastAsked = angular.copy(response);
	});
	$http.get("QandAServlet/UserLastAnswerdAnswers", parameters)
	.success(function(response) {
		$scope.user.lastAnswered = angular.copy(response);
	});
	
}]);

mainPageApp.controller('leaderboardCtrl', ['$scope', '$http', function($scope, $http) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	var parameter = { params: { pageNum: $scope.pageNum,} };
	$scope.updateLeaderboard = function(){
		$http.get("UserServlet/GetAllUsersInfo", parameter)
		.success(function(response){
			$scope.allUsers = response.users;
			var totalUsers = parseInt(response.numUsers);
			$scope.maxPageNum = parseInt((totalUsers-1)/20) + 1;
		});
	};
	
	$scope.updateLeaderboard();
	
	$scope.nextPage = function(){
		if ($scope.pageNum < $scope.maxPageNum){
			$scope.pageNum += 1;
			parameter = { params: { pageNum: $scope.pageNum,} };
			$scope.updateLeaderboard();
		}
	}
	
	$scope.prevPage = function(){
		if ($scope.pageNum > 1){
			$scope.pageNum -= 1;
			parameter = { params: { pageNum: $scope.pageNum,} };
			$scope.updateLeaderboard();
		}	
	}
}]);


mainPageApp.controller('topicsCtrl', ['$scope', '$http', function($scope, $http) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	$scope.tagsOnPage = 50;
	var parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
	
	$scope.updateTopics = function(){
		$http.get("QandAServlet/QuestionTopicsByTpop", parameter)
		.success(function(response){
			$scope.topics = response.topics;
			$scope.maxPageNum = parseInt((response.numTopics-1)/$scope.tagsOnPage) + 1;
			$scope.shuffleArray($scope.topics);
			// calculate average popularity score for the tag cloud
			$scope.tPopAvg = 0;
			for (var i = 0; i<$scope.topics.length; i++){
				$scope.tPopAvg += $scope.topics[i].tPop;
			}
			$scope.tPopAvg /= $scope.topics.length;
		});
	};
	
	$scope.updateTopics();
	
	$scope.nextPage = function(){
		if ($scope.pageNum < $scope.maxPageNum){
			$scope.pageNum += 1;
			parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
			$scope.updateTopics();
		}
	}
	
	$scope.prevPage = function(){
		if ($scope.pageNum > 1){
			$scope.pageNum -= 1;
			parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
			$scope.updateTopics();
		}	
	}
	
	//Randomize array element order in-place.
	$scope.shuffleArray = function (array) {
	    for (var i = array.length - 1; i > 0; i--) {
	        var j = Math.floor(Math.random() * (i + 1));
	        var temp = array[i];
	        array[i] = array[j];
	        array[j] = temp;
	    }
	    return array;
	}
	
	$scope.topicCloudStyle = function(topicPop){
		var minFontSize = 100;
		var maxFontSize = 600;
		var topicPopAboveAvgPer = (topicPop / $scope.tPopAvg) * 100;
		if (topicPopAboveAvgPer > maxFontSize){
			topicPopAboveAvgPer = maxFontSize;
		}
		if (topicPopAboveAvgPer < minFontSize){
			topicPopAboveAvgPer = minFontSize;
		}
		return {"font-size" : topicPopAboveAvgPer+"%"};
	};
}]);
