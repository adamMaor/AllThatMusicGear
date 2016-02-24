/**
 * 
 */

angular.module('mainPageApp').controller('questions', ['$scope', '$http', '$location',function($scope, $http, $location) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	
	var url = $location.path();
	if (url == "/newquestions"){
		$scope.questionMode = "NewQuestions";
		$scope.Title = "New Questions:";
		
		// answerBoxOpen - will be used as an indicator user isn't answering any questions at the moment
		// and new questions page can be refreshed
		$scope.answerBoxOpen = 0;
		
		$scope.callbackNewquestions = function (){
			if ($scope.answerBoxOpen == 0){
				$scope.updateQuestions();
			}
		}
		
		setInterval($scope.callbackNewquestions, 3000);
	}
	else if (url == "/topquestions"){
		$scope.questionMode = "AllQuestions";
		$scope.Title = "Top Questions:";
	}
	else {
		$scope.Topic = "";
		$scope.Topic = $location.hash();
		$scope.questionMode = "QuestionsByTopic";
		$scope.Title = "Questions By Topic - " + $scope.Topic;
	}
	var parameter = { params: { topic: $scope.Topic, pageNum: $scope.pageNum,} };
	$scope.questions = [];
	
	$scope.updateQuestions = function(){
		$scope.answerBoxOpen = 0;
		$http.get("QandAServlet/" + $scope.questionMode, parameter)
		.success(function(response) {
			$scope.questions = response.questions;				
			var totalQustions = parseInt(response.numQuestion);
			$scope.maxPageNum = parseInt((totalQustions-1)/20) + 1;
			$scope.questionCount = totalQustions;
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
	
	$scope.toggleAnswerBox = function(){
		if (this.showAnswerBox){
			$scope.answerBoxOpen--;			
		}
		else {
			$scope.answerBoxOpen++;
		}
		this.showAnswerBox = !this.showAnswerBox;
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
		
		$scope.answerBoxOpen--;
		this.aText = ""
		this.showAnswerBox = false;
		
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