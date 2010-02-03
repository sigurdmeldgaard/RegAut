;;;###autoload
(defun apply-replace-regexp-alist (edit-list &optional start end fixedcase literal)
  "Apply EDIT-LIST to the current buffer, and return the total count of changes.
Each edit is a regexp and a replacement for that regexp.
The edits are applied in order, to the whole buffer; that is, the buffer is
edited throughout with the first one, then with the next one, and so on.
If START and END are specified, replace only between them.
See documentation of replace-match for further optional arguments."
  (unless start (setq start (point-min)))
  (cond
   ((null end)
    (setq end (point-max-marker)))
   ((integerp end)
    (setq end (copy-marker end))))
  (let ((count 0))
    (save-excursion
      (while edit-list
	(goto-char start)
	(let* ((edit (car edit-list))
	       (regexp (car edit))
	       (replacement (cdr edit)))
	  (while (re-search-forward regexp end t)
	    (replace-match replacement fixedcase literal)
	    (setq count (1+ count))))
	(setq edit-list (cdr edit-list))))
    count))

(defun convert(beg end)
  (interactive "r")
  (apply-replace-regexp-alist
	 '((""."\\\\delta ");δ") 
	   (""."\\\\in ");∈") 
	   (""."\\\\Sigma ");Σ") 
	   (""."\\\\subseteq ");⊆") 
	   (""."\\\\times ");⨯") 
	   (""."\\\\rightarrow ");→") 
	   (""."\\\\Lambda ");Λ") 
	   (""."\\\\neq ");≠") 
	   (""."\\\\exists ");∃") 
	   (""."\\\\wedge ");∧") 
	   (""."\\\\not\\\\in ");∉") 
	   (""."\\\\vee ");∨")
	   (""."\\\\forall ")
	   (""."\\\\Leftrightarrow")
	   (""."\\\\geq ")
	   (""."\\\\Rightarrow ")
	   (""."\\\\Leftarrow ")
	   (""."\\\\cap ")
	   (""."\\\\equiv ")
	   (""."\\\\cup ")
           (" "." ")
           ("­"."-")
           (""."\\\\gamma ")
           (""."\\\\alpha ")
           (""."\\\\item ")
           ("Σ1"."\\\\Sigma_1")
           ("Σ2"."\\\\Sigma_2")
           ("Σ"."\\\\Sigma")
	   (""."\\\\leq "))));≥"))))

;;;###autoload
(defun apply-replace-regexp-alist-repeatedly (edit-list &optional fixedcase literal)
  "Like apply-replace-regexp-alist but keeps doing it until no further changes occur.
It is not guaranteed that this will ever terminate."
  (while (not (zerop (apply-replace-regexp-alist edit-list fixedcase literal)))))


;;; I'm not sure who wrote this; I don't think I did. JCGS
;;;###autoload
(defun replace-with-eval-result (regexp form)
  "Replace occurrences of REGEXP with the result of (eval FORM).
While FORM is evaluated, the variable \\& is bound to the whole
matched text, and the variables \\1, \\2 etc bound to the
corresponding one of the strings matched by bracketed expressions in
the regexp. The whole matched text is deleted from the buffer before
the form is evaluated. Thus, it is quite like replace-regexp, with
binding names being the same as the corresponding substitution markers
in the replacement string of replace-regexp."
  (interactive "sReplace regexp:
xReplace regexp %s with result of form: ")
  (while (re-search-forward regexp (point-max) t)
    (let ((m-d (match-data))
	  (matched-strings nil))
      (while m-d
	(setq matched-strings (cons (buffer-substring (car m-d) (cadr m-d))
				    matched-strings)
	      m-d (cddr m-d)))
      (progv
	  '(\\& \\1 \\2 \\3 \\4 \\5 \\6 \\7 \\8 \\9)
	  (nreverse matched-strings)
	(delete-region (match-beginning 0)
		       (match-end 0))
	(let ((result (save-excursion
			(save-window-excursion
			  (eval form)))))
	  (if (not (stringp result))
	      (setq result (prin1-to-string result 'no-escape)))
	  (insert result))))))


