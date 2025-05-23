% --- القواعد الديناميكية ---
:- dynamic(yes/1).
:- dynamic(no/1).

% --- الاحتمالات القبلية لكل مرض ---
% النسبة المئوية المبدئية لاحتمال الإصابة بالمرض
prior(covid19, 0.10).        % فيروس كورونا
prior(flu, 0.15).            % الإنفلونزا
prior(cold, 0.20).           % نزلة برد
prior(asthma, 0.05).         % الربو
prior(hypertension, 0.12).   % ارتفاع ضغط الدم
prior(diabetes, 0.13).       % مرض السكري
prior(bronchitis, 0.07).     % التهاب الشعب الهوائية
prior(pneumonia, 0.09).      % الالتهاب الرئوي
prior(migraine, 0.11).       % الصداع النصفي
prior(anemia, 0.08).         % فقر الدم
prior(sinusitis, 0.06).      % التهاب الجيوب الأنفية
prior(strep_throat, 0.05).   % التهاب الحلق البكتيري

% --- احتمالية وجود الأعراض لكل مرض ---
% likelihood(مرض، عرض، احتمال)

% أعراض الإنفلونزا
likelihood(flu, fever, 0.9).
likelihood(flu, cough, 0.8).
likelihood(flu, sore_throat, 0.7).
likelihood(flu, runny_nose, 0.6).
likelihood(flu, shortness_of_breath, 0.3).
likelihood(flu, wheezing, 0.2).
likelihood(flu, headache, 0.5).
likelihood(flu, dizziness, 0.4).
likelihood(flu, blurred_vision, 0.3).

% أعراض كورونا
likelihood(covid19, fever, 0.95).
likelihood(covid19, cough, 0.9).
likelihood(covid19, sore_throat, 0.4).
likelihood(covid19, runny_nose, 0.2).
likelihood(covid19, shortness_of_breath, 0.85).
likelihood(covid19, wheezing, 0.6).
likelihood(covid19, headache, 0.65).
likelihood(covid19, dizziness, 0.5).
likelihood(covid19, blurred_vision, 0.35).

% أعراض نزلة البرد
likelihood(cold, fever, 0.4).
likelihood(cold, cough, 0.7).
likelihood(cold, sore_throat, 0.8).
likelihood(cold, runny_nose, 0.9).
likelihood(cold, shortness_of_breath, 0.1).
likelihood(cold, wheezing, 0.1).
likelihood(cold, headache, 0.4).
likelihood(cold, dizziness, 0.3).
likelihood(cold, blurred_vision, 0.2).

% أعراض الربو
likelihood(asthma, cough, 0.6).
likelihood(asthma, shortness_of_breath, 0.95).
likelihood(asthma, wheezing, 0.8).

% أعراض ارتفاع ضغط الدم
likelihood(hypertension, headache, 0.5).
likelihood(hypertension, dizziness, 0.6).
likelihood(hypertension, blurred_vision, 0.4).

% أعراض السكري
likelihood(diabetes, blurred_vision, 0.5).
likelihood(diabetes, dizziness, 0.6).
likelihood(diabetes, fatigue, 0.7).
likelihood(diabetes, increased_thirst, 0.8).

% أعراض التهاب الشعب الهوائية
likelihood(bronchitis, cough, 0.9).
likelihood(bronchitis, chest_pain, 0.6).
likelihood(bronchitis, wheezing, 0.7).

% أعراض الالتهاب الرئوي
likelihood(pneumonia, fever, 0.85).
likelihood(pneumonia, cough, 0.8).
likelihood(pneumonia, chest_pain, 0.7).
likelihood(pneumonia, shortness_of_breath, 0.8).

% أعراض الصداع النصفي
likelihood(migraine, headache, 0.95).
likelihood(migraine, nausea, 0.7).
likelihood(migraine, sensitivity_to_light, 0.6).

% أعراض فقر الدم
likelihood(anemia, fatigue, 0.85).
likelihood(anemia, dizziness, 0.7).
likelihood(anemia, pale_skin, 0.6).

% أعراض التهاب الجيوب الأنفية
likelihood(sinusitis, headache, 0.6).
likelihood(sinusitis, nasal_congestion, 0.9).
likelihood(sinusitis, facial_pain, 0.8).

% أعراض التهاب الحلق البكتيري
likelihood(strep_throat, sore_throat, 0.95).
likelihood(strep_throat, fever, 0.7).
likelihood(strep_throat, swollen_lymph_nodes, 0.6).

% --- قائمة الأعراض الكاملة ---
symptom(fever).
symptom(cough).
symptom(sore_throat).
symptom(runny_nose).
symptom(shortness_of_breath).
symptom(wheezing).
symptom(headache).
symptom(dizziness).
symptom(blurred_vision).
symptom(chest_pain).
symptom(fatigue).
symptom(increased_thirst).
symptom(nausea).
symptom(sensitivity_to_light).
symptom(pale_skin).
symptom(nasal_congestion).
symptom(facial_pain).
symptom(swollen_lymph_nodes).

% --- تتبع الأعراض المعروفة ---
known(S) :- yes(S) ; no(S).

% --- إرجاع قائمة بالأعراض غير المُجاب عنها ---
ask_all_symptoms(Symptoms) :-
    findall(S, (symptom(S), \+ known(S)), Symptoms).

% --- حساب الاحتمالات ---
posterior(Disease, Posterior) :-
    prior(Disease, Prior),
    findall(S, yes(S), YesSymptoms),
    compute_likelihood(YesSymptoms, Disease, Likelihood),
    Posterior is Prior * Likelihood.

compute_likelihood([], _, 1).
compute_likelihood([S|T], D, Result) :-
    (   likelihood(D, S, P) -> true ; P = 0.01),
    compute_likelihood(T, D, R),
    Result is P * R.

normalize(Scores, NormalizedScores) :-
    sum_probs(Scores, Total),
    (   Total > 0 ->
        maplist(normalize_prob(Total), Scores, NormalizedScores)
    ;   NormalizedScores = Scores).

normalize_prob(Total, D-P, D-NP) :-
    NP is (P / Total) * 100.

sum_probs([], 0).
sum_probs([_-P|T], Total) :-
    sum_probs(T, Rest),
    Total is P + Rest.

reset_answers :-
    retractall(yes(_)),
    retractall(no(_)).

% --- التشخيص النهائي وإرجاع النتائج مرتبة ---
diagnose_results(SortedResults) :-
    findall(D-P, posterior(D, P), Scores),
    normalize(Scores, NormalizedScores),
    sort(2, @>=, NormalizedScores, SortedResults).

% --- قائمة الأعراض مع الترجمة ---
% fever                الحمى
% cough                السعال
% sore_throat          التهاب الحلق
% runny_nose           سيلان الأنف
% shortness_of_breath  ضيق في التنفس
% wheezing             صفير في التنفس
% headache             صداع
% dizziness            دوخة
% blurred_vision       رؤية مشوشة
% chest_pain           ألم في الصدر
% fatigue              تعب / إرهاق
% increased_thirst     العطش الزائد
% nausea               غثيان
% sensitivity_to_light الحساسية للضوء
% pale_skin            شحوب في الجلد
% nasal_congestion     احتقان الأنف
% facial_pain          ألم في الوجه
% swollen_lymph_nodes  تورم الغدد اللمفاوية
