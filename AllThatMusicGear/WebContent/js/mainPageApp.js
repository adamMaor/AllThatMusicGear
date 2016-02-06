var checkLogin = function () {
	$.get("http://localhost:8080/AllThatMusicGear/UserServlet/GetUserInfo", function(data, status){
		if (data.nickName == "null"){
			window.location.href = '/AllThatMusicGear/login.html';			
		}
	});
};

var mainPageApp = angular.module('mainPageApp',[]);

mainPageApp.directive('header', function(){
	return {
	    templateUrl: "/AllThatMusicGear/header.html",
	};
});

mainPageApp.directive('footer', function(){
	return {
	    templateUrl: "/AllThatMusicGear/footer.html",
	};
});


mainPageApp.controller('navBarController', ['$scope', '$http', function($scope, $http) {
	$scope.loggedInUserNickName = "Welcome ";
	$scope.loggedInUserPhotoURL = "media/defaultIcon.png"; //TODO make default to insert to DB when no url provided
	
	$scope.loggedInUserInfo = function(){
		$http.get(
				"http://localhost:8080/AllThatMusicGear/UserServlet/GetUserInfo"
		).success(function(response) {
			if (response.nickName != "null"){
				$scope.loggedInUserNickName = "Welcome " + response.nickName + " ";	
				if (response.photoURL != "null" && response.photoURL !=""){
					$scope.loggedInUserPhotoURL = response.photoURL;
				}
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
			$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/InsertQuestion", parameters);
			$scope.resetQFields();
			$('#askQuestionModal').modal('hide');
		}
	
	$scope.resetQFields = function(){
		$scope.qText = "";
		$scope.qTopics = "";
	}			 
 }]);

mainPageApp.controller('questions', ['$scope', '$http', function($scope, $http) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	
	$scope.updateQuestions = function(){
		$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/AllQuestions")
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
				$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/AnswersOfQ", parameters)
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
		$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/UpdateQuestion", parameters)
		.success(function(response) {
			for (var i = 0; i < $scope.questions.length; i++){
				if ($scope.questions[i].qID == qID){
					$scope.questions[i].qVotingScore += changeScore;
					$scope.questions.sort(function(a,b)
							{
								return b.qVotingScore - a.qVotingScore;
							});
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
		$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/UpdateAnswer", parameters)
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
		$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/InsertAnswer", parameters)
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
			$http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/AnswersOfQ", parameters)
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

mainPageApp.controller('submitAnswer', ['$scope', '$http', function($scope, $http) {
	
	
}]);




