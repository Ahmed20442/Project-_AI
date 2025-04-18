% قاعدة لتخزين التشخيصات لكل مريض
:- dynamic diagnosis/2.

go :-
    write('What is the patient\'s name? '),
    read(Patient),
    ( diagnosis(Patient, Disease) ->
        write_list([Patient, ' was already diagnosed with ', Disease, '.']), nl
    ;
        retractall(yes(_)),
        retractall(no(_)),
        ( hypothesis(Patient, Disease) ->
            assert(diagnosis(Patient, Disease)),
            write_list([Patient, ', probably has ', Disease, '.']), nl
        ;
            write('Sorry, I don\'t seem to be able to'), nl,
            write('diagnose the disease.'), nl
        )
    ).


symptom(Patient,fever) :-
verify(Patient," have a fever (y/n) ?").
symptom(Patient,rash) :-
verify(Patient," have a rash (y/n) ?").
symptom(Patient,headache) :-
verify(Patient," have a headache (y/n) ?").
symptom(Patient,runny_nose) :-
verify(Patient," have a runny_nose (y/n) ?").
symptom(Patient,conjunctivitis) :-
verify(Patient," have a conjunctivitis (y/n) ?").
symptom(Patient,cough) :-
verify(Patient," have a cough (y/n) ?").
symptom(Patient,body_ache) :-
verify(Patient," have a body_ache (y/n) ?").
symptom(Patient,chills) :-
verify(Patient," have a chills (y/n) ?").
symptom(Patient,sore_throat) :-
verify(Patient," have a sore_throat (y/n) ?").
symptom(Patient,sneezing) :-
verify(Patient," have a sneezing (y/n) ?").
symptom(Patient,swollen_glands) :-
verify(Patient," have a swollen_glands (y/n) ?").
symptom(Patient,shortness_of_breath) :-
    verify(Patient," have shortness of breath (y/n) ?").

symptom(Patient,loss_of_taste_or_smell) :-
    verify(Patient," have loss of taste or smell (y/n) ?").

symptom(Patient,fatigue) :-
    verify(Patient," have fatigue (y/n) ?").
symptom(Patient,excessive_thirst) :-
    verify(Patient," have excessive thirst (y/n) ?").
symptom(Patient,frequent_urination) :-
    verify(Patient," have frequent urination (y/n) ?").
symptom(Patient,increased_hunger) :-
    verify(Patient," have increased hunger (y/n) ?").
symptom(Patient,unexplained_weight_loss) :-
    verify(Patient," have unexplained weight loss (y/n) ?").
symptom(Patient,dizziness) :-
    verify(Patient," have dizziness (y/n) ?").
symptom(Patient,blurred_vision) :-
    verify(Patient," have blurred vision (y/n) ?").
symptom(Patient,chest_pain) :-
    verify(Patient," have chest pain (y/n) ?").

symptom(Patient,persistent_cough) :-
    verify(Patient," have a persistent cough (y/n) ?").
symptom(Patient,coughing_up_blood) :-
    verify(Patient," have coughing up blood (y/n) ?").
symptom(Patient,breast_lump) :-
    verify(Patient," have a lump in the breast (y/n) ?").
symptom(Patient,breast_pain) :-
    verify(Patient," have pain in the breast (y/n) ?").
symptom(Patient,nipple_discharge) :-
    verify(Patient," have nipple discharge (y/n) ?").
symptom(Patient,skin_changes) :-
    verify(Patient," have skin changes on the breast (y/n) ?").
symptom(Patient,abdominal_pain) :-
    verify(Patient," have abdominal pain (y/n) ?").
symptom(Patient,blood_in_stool) :-
    verify(Patient," have blood in stool (y/n) ?").
symptom(Patient,constipation_or_diarrhea) :-
    verify(Patient," have constipation or diarrhea (y/n) ?").
symptom(Patient,weight_loss) :-
    verify(Patient," have unexplained weight loss (y/n) ?").
symptom(Patient,frequent_infections) :-
    verify(Patient," have frequent infections (y/n) ?").
symptom(Patient,bruising_or_bleeding) :-
    verify(Patient," have unusual bruising or bleeding (y/n) ?").
symptom(Patient,pale_skin) :-
    verify(Patient," have pale skin (y/n) ?").

symptom(Patient,abdominal_swelling) :-
    verify(Patient," have abdominal swelling (y/n) ?"). % انتفاخ في البطن
symptom(Patient,jaundice) :-
    verify(Patient," have yellowing of skin or eyes (jaundice) (y/n) ?"). % اصفرار الجلد أو العينين
symptom(Patient,nausea) :-
    verify(Patient," have nausea or vomiting (y/n) ?"). % غثيان أو قيء

symptom(Patient,frequent_urination) :-
    verify(Patient," have frequent urination (y/n) ?"). % كثرة التبول
symptom(Patient,weak_urine_flow) :-
    verify(Patient," have weak urine flow (y/n) ?"). % ضعف تدفق البول
symptom(Patient,pelvic_discomfort) :-
    verify(Patient," have pelvic discomfort (y/n) ?"). % انزعاج في منطقة الحوض
symptom(Patient,blood_in_urine_or_semen) :-
    verify(Patient," have blood in urine or semen (y/n) ?"). % دم في البول أو السائل المنوي

% أعراض سرطان الجلد
symptom(Patient,skin_lesions) :-
    verify(Patient," have skin lesions or unusual moles (y/n) ?"). % شامة أو بقعة غير طبيعية على الجلد
symptom(Patient,itching_or_bleeding_lesion) :-
    verify(Patient," have an itching or bleeding skin lesion (y/n) ?"). % حكة أو نزيف في الجلد
symptom(Patient,skin_discoloration) :-
    verify(Patient," have patches of skin discoloration (y/n) ?").

ask(Patient,Question) :-
	write(Patient),write(', do you'),write(Question),
	read(N),
	( (N == yes ; N == y)
      ->
       assert(yes(Question)) ;
       assert(no(Question)), fail).

:- dynamic yes/1,no/1.

verify(P,S) :-
   (yes(S) -> true ;
    (no(S) -> fail ;
     ask(P,S))).

undo :- retract(yes(_)),fail.
undo :- retract(no(_)),fail.
undo.



hypothesis(Patient,german_measles) :-
symptom(Patient,fever),
symptom(Patient,headache),
symptom(Patient,runny_nose),
symptom(Patient,rash).

hypothesis(Patient,common_cold) :-
symptom(Patient,headache),
symptom(Patient,sneezing),
symptom(Patient,sore_throat),
symptom(Patient,runny_nose),
symptom(Patient,chills).

hypothesis(Patient,measles) :-
symptom(Patient,cough),
symptom(Patient,sneezing),
symptom(Patient,runny_nose).

hypothesis(Patient,flu) :-
symptom(Patient,fever),
symptom(Patient,headache),
symptom(Patient,body_ache),
symptom(Patient,conjunctivitis),
symptom(Patient,chills),
symptom(Patient,sore_throat),
symptom(Patient,runny_nose),
symptom(Patient,cough).



hypothesis(Patient,mumps) :-
symptom(Patient,fever),
symptom(Patient,swollen_glands).

hypothesis(Patient,chicken_pox) :-
symptom(Patient,fever),
symptom(Patient,chills),
symptom(Patient,body_ache),
symptom(Patient,rash).

hypothesis(Patient,covid_19) :-
    symptom(Patient,fever),
    symptom(Patient,cough),
    symptom(Patient,shortness_of_breath),
    symptom(Patient,loss_of_taste_or_smell).

hypothesis(Patient,diabetes) :-
    symptom(Patient,excessive_thirst),
    symptom(Patient,frequent_urination),
    symptom(Patient,increased_hunger),
    symptom(Patient,unexplained_weight_loss).

hypothesis(Patient,hypertension) :-
    symptom(Patient,headache),
    symptom(Patient,dizziness),
    symptom(Patient,blurred_vision),
    symptom(Patient,chest_pain).

hypothesis(Patient,lung_cancer) :-
    symptom(Patient,persistent_cough),
    symptom(Patient,shortness_of_breath),
    symptom(Patient,chest_pain),
    symptom(Patient,coughing_up_blood).

hypothesis(Patient,breast_cancer) :-
    symptom(Patient,breast_lump),
    symptom(Patient,breast_pain),
    symptom(Patient,nipple_discharge),
    symptom(Patient,skin_changes).

hypothesis(Patient,colorectal_cancer) :-
    symptom(Patient,abdominal_pain),
    symptom(Patient,blood_in_stool),
    symptom(Patient,constipation_or_diarrhea),
    symptom(Patient,weight_loss).

hypothesis(Patient,leukemia) :-
    symptom(Patient,frequent_infections),
    symptom(Patient,bruising_or_bleeding),
    symptom(Patient,pale_skin),
    symptom(Patient,fatigue).

% تشخيص سرطان الكبد
hypothesis(Patient,liver_cancer) :-
    symptom(Patient,abdominal_swelling),
    symptom(Patient,jaundice),
    symptom(Patient,nausea),
    symptom(Patient,fatigue).

% تشخيص سرطان البروستاتا
hypothesis(Patient,prostate_cancer) :-
    symptom(Patient,frequent_urination),
    symptom(Patient,weak_urine_flow),
    symptom(Patient,pelvic_discomfort),
    symptom(Patient,blood_in_urine_or_semen).

% تشخيص سرطان الجلد
hypothesis(Patient,skin_cancer) :-
    symptom(Patient,skin_lesions),
    symptom(Patient,itching_or_bleeding_lesion),
    symptom(Patient,skin_discoloration).


write_list([]).
write_list([Term| Terms]) :-
write(Term),
write_list(Terms).

response(Reply) :-
get_single_char(Code),
put_code(Code), nl,
char_code(Reply, Code).
