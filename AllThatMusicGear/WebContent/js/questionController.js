/**
 * 
 */

angular.module('mainPageApp').controller('questions', ['$scope', '$http', '$location',function($scope, $http, $location) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	
	// get data from url after first "#/" - used to decide which question page to show
	var url = $location.path();
	if (url == "/newquestions"){
		$scope.questionMode = "NewQuestions";
		$scope.Title = "New Questions:";
		
		// answerBoxOpen - will be used as an indicator user isn't answering any questions at the moment
		// and new questions page can be refreshed
		$scope.answerBoxOpen = 0;
		
		// callback function in newquestions page to poll for changes in what should be displayed
		$scope.callbackNewquestions = function (){
			if ($scope.answerBoxOpen == 0){
				$scope.updateQuestions();
			}
		}
		
		// set callback function to occur every 3 seconds
		// will update when user answered a question or a 
		// new questions was added by some user
		setInterval($scope.callbackNewquestions, 3000);
	}
	else if (url == "/topquestions"){
		$scope.questionMode = "AllQuestions";
		$scope.Title = "Top Questions:";
	}
	else {
		// if it's not new questions or top questions, its a questions by topic page
		$scope.Topic = "";
		// the topic - will be shown after the second #
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
	
	// function tries to vote for current loggin in user, triggered by button press
	// on one of the vote buttons of a question, if successfull a popover will show and color will
	// change, else an unsuccessful popover will show
	$scope.voteQuestion = function(qID, changeScore, event)
	{
		checkLogin();
		// get button element in order to add a popover and/or change color depending on success of action
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
	
	// add color to button on successful voting and populate 
	// the popover with success/fail status for vote
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
	// -2 : user's own submission can't vote
	// -1 : user voted down a submission
	//  0 : user hasn't voted yet
	//  1 : user has upvoted a submission
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
	
	// increase/decrease counter and toggle answerbox
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
		// since the answerbox will close
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
			// update answers to question from server after successful insertion of answer
			$http.get("QandAServlet/AnswersOfQ", parameters)
			.success(function(response) {
				if(response[0] !== undefined){
					// find the question that was answered and update its answers
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

// directive attached to voting buttons in order to change their state (disabled) or color
// according to what the current user previously voted
mainPageApp.directive('voteButton', function ($compile) {
 return {
 	restrict: 'C',
 	link: function (scope, element, attrs) {
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
	    	// add the appropriate class to mark buttons
	    	$(element).addClass(buttonClass);
	    	
	    	// make buttons that shouldn't be clicked disabled by adding disabled attribute
	    	if (buttonClass.indexOf("disabled") > -1 
	    			|| buttonClass.indexOf("primary") > -1 
	    			|| buttonClass.indexOf("danger") > -1){
	    		element.attr("disabled", "disabled");
	    	}
 },};
});