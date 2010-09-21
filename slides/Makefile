SEMS=1_seminar 2_seminar 3_seminar

svgfigs=\
1_seminar_even_num_zeroes.svg\
1_seminar_fried_egg.svg\
1_seminar_pacman.svg\
1_seminar_odd_num_ones.svg\
1_seminar_everything.svg\
1_seminar_nothing.svg\
1_seminar_only_a.svg\
1_seminar_even_a_odd_b.svg\
1_seminar_even_num_zeroes_label.svg\
1_seminar_even_num_zeroes_label2.svg\
1_seminar_union.svg\
1_seminar_union_inv.svg\
1_seminar_union2.svg\
1_seminar_ends_with_zero.svg\
1_seminar_togmodel_tog.svg\
1_seminar_togmodel_kontrol.svg\
1_seminar_togmodel_togFA.svg\
1_seminar_togmodel_sikkerhed.svg\
1_seminar_togmodel_bom.svg\
2_seminar_convert.svg\
2_seminar_quiz_NFA.svg\
2_seminar_lambdaelim.svg

dotfigs=\
2_seminar_quiz_nfa_lambda.dot

fromsvg=$(addprefix images/,$(svgfigs:%.svg=%.pdf))
fromdot=$(addprefix images/,$(dotfigs:%.dot=%.png))

purefigs=\
1_seminar_bog.jpg\
1_seminar_futtog.png\
1_seminar_real_egg.jpg\
1_seminar_skuffe.jpg\
2_seminar_basis_a.png\
2_seminar_basis_emptyset.png\
2_seminar_basis_lambda.png\
2_seminar_equivclassesodd.png\
2_seminar_equivclasses.png\
2_seminar_equivclassessigma.png\
2_seminar_equiv.png\
2_seminar_fixpoint.png\
2_seminar_kleene_1_00plus1.png\
2_seminar_kleene_1_add.png\
2_seminar_kleene_1_ex_00plus1.png\
2_seminar_kleene_1_ex_00plus1star10star.png\
2_seminar_kleene_1_ex_00plus1star.png\
2_seminar_kleene_1_ex_00.png\
2_seminar_kleene_1_ex_0.png\
2_seminar_kleene_1_ex_10.png\
2_seminar_kleene_1_ex_10star.png\
2_seminar_kleene_1_ex_1.png\
2_seminar_kleene_1_star.png\
2_seminar_kleene_1_times.png\
2_seminar_kleene_2_ex.png\
2_seminar_L42_NFA.png\
2_seminar_min_ex_1.png\
2_seminar_min_ex_2.png\
2_seminar_min_ex_3.png\
2_seminar_minimizeequiv.png\
2_seminar_minimize.png\
2_seminar_myhillfa.png\
2_seminar_nfalambda.png\
2_seminar_odd1.png\
2_seminar_quiz_nfa_lambda.png\
biblio.png\
lunch.jpg\
pumping.pdf\
klasser.pdf\
xkcd.png

$(fromsvg): %.pdf: %.svg
	inkscape $< --export-area-drawing --export-pdf=$@

$(fromdot): %.png: %.dot
	dot $< -Tpng -o $@

figs=$(fromsvg) $(fromdot) $(addprefix images/,$(purefigs))

$(F).bbl: references.bib
	bibtex $(F)

$(addsuffix .pdf, $(SEMS)): %.pdf: %.tex $(figs)
	pdflatex $<


$(addsuffix _handout.tex, $(SEMS)): %_handout.tex: %.tex
	sed 's_documentclass\[\]_documentclass[handout]_'\
 $(<) > $(@:.pdf=.tex)

$(addsuffix _handout.pdf, $(SEMS)): %_handout.pdf: %_handout.tex $(figs)
	pdflatex --interaction=nonstopmode $(<)
	pdflatex --interaction=nonstopmode $(<)



